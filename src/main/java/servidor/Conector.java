package servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mensajeria.Comando;
import mensajeria.PaqueteInventario;
import mensajeria.PaqueteItem;
import mensajeria.PaqueteMochila;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

public class Conector {

	private String url = "primeraBase.bd";
	Connection connect;

	public void connect() {
		try {
			Servidor.log.append("Estableciendo conexión con la base de datos..." + System.lineSeparator());
			connect = DriverManager.getConnection("jdbc:sqlite:" + url);
			Servidor.log.append("Conexión con la base de datos establecida con �xito." + System.lineSeparator());
		} catch (SQLException ex) {
			Servidor.log.append("Fallo al intentar establecer la conexi�n con la base de datos. " + ex.getMessage()
					+ System.lineSeparator());
		}
	}

	public void close() {
		try {
			connect.close();
		} catch (SQLException ex) {
			Servidor.log.append("Error al intentar cerrar la conexión con la base de datos." + System.lineSeparator());
			Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean registrarUsuario(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			PreparedStatement st1 = connect.prepareStatement("SELECT * FROM registro WHERE usuario= ? ");
			st1.setString(1, user.getUsername());
			result = st1.executeQuery();

			if (!result.next()) {

				PreparedStatement st = connect
						.prepareStatement("INSERT INTO registro (usuario, password, idPersonaje) VALUES (?,?,?)");
				st.setString(1, user.getUsername());
				st.setString(2, user.getPassword());
				st.setInt(3, user.getIdPj());
				st.execute();
				Servidor.log.append("El usuario " + user.getUsername() + " se ha registrado." + System.lineSeparator());
				return true;
			} else {
				Servidor.log.append(
						"El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());
				return false;
			}
		} catch (SQLException ex) {
			Servidor.log.append("Eror al intentar registrar el usuario " + user.getUsername() + System.lineSeparator());
			System.err.println(ex.getMessage());
			return false;
		}
	}

	public boolean registrarPersonaje(PaquetePersonaje paquetePersonaje, PaqueteUsuario paqueteUsuario) {

		try {

			// Registro al personaje en la base de datos
			PreparedStatement stRegistrarPersonaje = connect.prepareStatement(
					"INSERT INTO personaje (idInventario, idMochila,casta,raza,fuerza,destreza,inteligencia,saludTope,energiaTope,nombre,experiencia,nivel,idAlianza) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);
			stRegistrarPersonaje.setInt(1, -1);
			stRegistrarPersonaje.setInt(2, -1);
			stRegistrarPersonaje.setString(3, paquetePersonaje.getCasta());
			stRegistrarPersonaje.setString(4, paquetePersonaje.getRaza());
			stRegistrarPersonaje.setInt(5, paquetePersonaje.getFuerza());
			stRegistrarPersonaje.setInt(6, paquetePersonaje.getDestreza());
			stRegistrarPersonaje.setInt(7, paquetePersonaje.getInteligencia());
			stRegistrarPersonaje.setInt(8, paquetePersonaje.getSaludTope());
			stRegistrarPersonaje.setInt(9, paquetePersonaje.getEnergiaTope());
			stRegistrarPersonaje.setString(10, paquetePersonaje.getNombre());
			stRegistrarPersonaje.setInt(11, 0);
			stRegistrarPersonaje.setInt(12, 1);
			stRegistrarPersonaje.setInt(13, -1);
			stRegistrarPersonaje.execute();

			// Recupero la última key generada
			ResultSet rs = stRegistrarPersonaje.getGeneratedKeys();
			if (rs != null && rs.next()) {

				// Obtengo el id
				int idPersonaje = rs.getInt(1);

				// Le asigno el id al paquete personaje que voy a devolver
				paquetePersonaje.setId(idPersonaje);

				// Le asigno el personaje al usuario
				PreparedStatement stAsignarPersonaje = connect
						.prepareStatement("UPDATE registro SET idPersonaje=? WHERE usuario=? AND password=?");
				stAsignarPersonaje.setInt(1, idPersonaje);
				stAsignarPersonaje.setString(2, paqueteUsuario.getUsername());
				stAsignarPersonaje.setString(3, paqueteUsuario.getPassword());
				stAsignarPersonaje.execute();

				// Por ultimo registro el inventario y la mochila
				if (this.registrarInventarioMochila(idPersonaje)) {
					Servidor.log.append("El usuario " + paqueteUsuario.getUsername() + " ha creado el personaje "
							+ paquetePersonaje.getId() + System.lineSeparator());
					return true;
				} else {
					Servidor.log.append(
							"Error al registrar la mochila y el inventario del usuario " + paqueteUsuario.getUsername()
									+ " con el personaje" + paquetePersonaje.getId() + System.lineSeparator());
					return false;
				}
			}
			return false;

		} catch (SQLException e) {
			Servidor.log.append(
					"Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());
			e.printStackTrace();
			return false;
		}

	}

	public boolean registrarInventarioMochila(int idInventarioMochila) {
		try {
			// Preparo la consulta para el registro el inventario en la base de
			// datos
			PreparedStatement stRegistrarInventario = connect.prepareStatement(
					"INSERT INTO inventario(idInventario,manos1,manos2,pie,cabeza,pecho,accesorio) VALUES (?,-1,-1,-1,-1,-1,-1)");
			stRegistrarInventario.setInt(1, idInventarioMochila);

			// Preparo la consulta para el registro la mochila en la base de
			// datos
			PreparedStatement stRegistrarMochila = connect.prepareStatement(
					"INSERT INTO mochila(idMochila,item1,item2,item3,item4,item5,item6,item7,item8,item9,item10,item11,item12,item13,item14,item15,item16,item17,item18,item19,item20) VALUES(?,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)");
			stRegistrarMochila.setInt(1, idInventarioMochila);

			// Registro inventario y mochila
			stRegistrarInventario.execute();
			stRegistrarMochila.execute();

			// Le asigno el inventario y la mochila al personaje
			PreparedStatement stAsignarPersonaje = connect
					.prepareStatement("UPDATE personaje SET idInventario=?, idMochila=? WHERE idPersonaje=?");
			stAsignarPersonaje.setInt(1, idInventarioMochila);
			stAsignarPersonaje.setInt(2, idInventarioMochila);
			stAsignarPersonaje.setInt(3, idInventarioMochila);
			stAsignarPersonaje.execute();

			Servidor.log.append("Se ha registrado el inventario de " + idInventarioMochila + System.lineSeparator());
			return true;

		} catch (SQLException e) {
			Servidor.log.append("Error al registrar el inventario de " + idInventarioMochila + System.lineSeparator());
			e.printStackTrace();
			return false;
		}
	}

	public boolean loguearUsuario(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			// Busco usuario y contraseña
			PreparedStatement st = connect
					.prepareStatement("SELECT * FROM registro WHERE usuario = ? AND password = ? ");
			st.setString(1, user.getUsername());
			st.setString(2, user.getPassword());
			result = st.executeQuery();

			// Si existe inicio sesion
			if (result.next()) {
				Servidor.log
						.append("El usuario " + user.getUsername() + " ha iniciado sesi�n." + System.lineSeparator());
				return true;
			}

			// Si no existe informo y devuelvo false
			Servidor.log.append("El usuario " + user.getUsername()
					+ " ha realizado un intento fallido de inicio de sesi�n." + System.lineSeparator());
			return false;

		} catch (SQLException e) {
			Servidor.log
					.append("El usuario " + user.getUsername() + " fallo al iniciar sesi�n." + System.lineSeparator());
			e.printStackTrace();
			return false;
		}

	}

	public void actualizarPersonaje(PaquetePersonaje paquetePersonaje) {
		try {
			PreparedStatement stActualizarPersonaje = connect.prepareStatement(
					"UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=? "
							+ "  WHERE idPersonaje=?");

			stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
			stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
			stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
			stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
			stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
			stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
			stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
			stActualizarPersonaje.setInt(8, paquetePersonaje.getId());

			stActualizarPersonaje.executeUpdate();

			Servidor.log.append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con �xito."
					+ System.lineSeparator());

			this.actualizarInventario(paquetePersonaje.getPaqueteInventario());
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
			e.printStackTrace();
		}
	}

	public void actualizarInventario(PaqueteInventario paqueteInventario) {
		try {
			PreparedStatement stActualizarInventario = connect.prepareStatement(
					"UPDATE inventario SET manos1=?, manos2=?, pie=?, cabeza=?, pecho=?, accesorio=? WHERE idInventario=?");

		
			stActualizarInventario.setInt(1, paqueteInventario.getItems().get(1).getId());
			stActualizarInventario.setInt(2, paqueteInventario.getItems().get(2).getId());
			stActualizarInventario.setInt(3, paqueteInventario.getItems().get(3).getId());
			stActualizarInventario.setInt(4, paqueteInventario.getItems().get(4).getId());
			stActualizarInventario.setInt(5, paqueteInventario.getItems().get(5).getId());
			stActualizarInventario.setInt(6, paqueteInventario.getItems().get(6).getId());
			stActualizarInventario.setInt(7, paqueteInventario.getId());

			stActualizarInventario.executeUpdate();

			Servidor.log.append("El inventario " + paqueteInventario.getId() + " se ha actualizado con éxito."
					+ System.lineSeparator());

		} catch (SQLException e) {
			Servidor.log.append(
					"Fallo al intentar actualizar el inventario " + paqueteInventario.getId() + System.lineSeparator());
			e.printStackTrace();
		}
	}

	public PaquetePersonaje getPersonaje(PaqueteUsuario user) {
		ResultSet result = null;
		try {
			// Selecciono el personaje de ese usuario
			PreparedStatement st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, user.getUsername());
			result = st.executeQuery();

			// Obtengo el id
			int idPersonaje = result.getInt("idPersonaje");

			// Selecciono los datos del personaje
			PreparedStatement stSeleccionarPersonaje = connect
					.prepareStatement("SELECT * FROM personaje WHERE idPersonaje = ?");
			stSeleccionarPersonaje.setInt(1, idPersonaje);
			result = stSeleccionarPersonaje.executeQuery();

			// Obtengo los atributos del personaje
			PaquetePersonaje personaje = new PaquetePersonaje();
			personaje.setId(idPersonaje);
			personaje.setRaza(result.getString("raza"));
			personaje.setCasta(result.getString("casta"));
			personaje.setFuerza(result.getInt("fuerza"));
			personaje.setInteligencia(result.getInt("inteligencia"));
			personaje.setDestreza(result.getInt("destreza"));
			personaje.setEnergiaTope(result.getInt("energiaTope"));
			personaje.setSaludTope(result.getInt("saludTope"));
			personaje.setNombre(result.getString("nombre"));
			personaje.setExperiencia(result.getInt("experiencia"));
			personaje.setNivel(result.getInt("nivel"));

			personaje.setInventario(getInventario(result.getInt("idInventario")));
			personaje.setMochila(getMochila(result.getInt("idMochila")));

			// Devuelvo el paquete personaje con sus datos
			return personaje;

		} catch (

		SQLException ex) {
			Servidor.log
					.append("Fallo al intentar recuperar el personaje " + user.getUsername() + System.lineSeparator());
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
			ex.printStackTrace();
		}

		return new PaquetePersonaje();
	}

	public PaqueteInventario getInventario(int idInventario) {
		try {
			// Selecciono los datos del inventario
			PreparedStatement stSeleccionarInventario = connect.prepareStatement("SELECT * FROM Inventario AS INV "
					+ "LEFT JOIN Item AS It1 ON (INV.manos1 = It1.idItem)"
					+ "INNER JOIN TipoItem AS Ti1 ON (It1.idTipoItem = Ti1.idTipoItem)"
					+ "LEFT JOIN Item AS It2 ON (INV.manos2 = It2.idItem)"
					+ "INNER JOIN TipoItem AS Ti2 ON (It2.idTipoItem = Ti2.idTipoItem)"
					+ "LEFT JOIN Item AS It3 ON (INV.pie = It3.idItem)"
					+ "INNER JOIN TipoItem AS Ti3 ON (It3.idTipoItem = Ti3.idTipoItem)"
					+ "LEFT JOIN Item AS It4 ON (INV.cabeza = It4.idItem)"
					+ "INNER JOIN TipoItem AS Ti4 ON (It4.idTipoItem = Ti4.idTipoItem)"
					+ "LEFT JOIN Item AS It5 ON (INV.pecho = It5.idItem)"
					+ "INNER JOIN TipoItem AS Ti5 ON (It5.idTipoItem = Ti5.idTipoItem)"
					+ "LEFT JOIN Item AS It6 ON (INV.accesorio = It6.idItem)"
					+ "INNER JOIN TipoItem AS Ti6 ON (It6.idTipoItem = Ti6.idTipoItem)" + "WHERE idInventario = ?");
			stSeleccionarInventario.setInt(1, idInventario);

			ResultSet result = stSeleccionarInventario.executeQuery();
			Map<Integer, PaqueteItem> items = new HashMap<Integer, PaqueteItem>();
			if (result.next()) {
				int indice = 8; // Columna comienzo datos items
				for (int i = 1; i < 7; i++) {
					PaqueteItem item = new PaqueteItem();
					Servidor.log.append("Indice: " + result.getInt(indice));
					item.setId(result.getInt(indice));
					if (item.getId() > 0) {
						item.setNombre(result.getString(indice + 1));
						item.setIdTipo(result.getInt(indice + 2));
						item.setBonoAtaque(result.getInt(indice + 3));
						item.setTipo(result.getString(indice + 12));
						item.setBonoDefensa(result.getInt(indice + 4));
						item.setBonoMagia(result.getInt(indice + 5));
						item.setBonoSalud(result.getInt(indice + 6));
						item.setBonoEnergia(result.getInt(indice + 7));
						item.setFuerzaRequerida(result.getInt(indice + 8));
						item.setDestrezaRequerida(result.getInt(indice + 9));
						item.setInteligenciaRequerida(result.getInt(indice + 10));						
					}
					items.put(i, item);
					indice += 13; // Cantidad columnas por item
				}
			}
			return new PaqueteInventario(idInventario, items);
		} catch (SQLException ex) {
			Servidor.log.append("Fallo al intentar recuperar el inventario " + idInventario + System.lineSeparator());
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
			ex.printStackTrace();
		}
		return null;
	}

	public PaqueteMochila getMochila(int idMochila) {
		try {
			// Selecciono los datos de la Mochila
			PreparedStatement stSeleccionarMochila = connect.prepareStatement("SELECT * FROM Mochila AS M "
					+ "LEFT JOIN Item AS It1 ON (M.item1 = It1.idItem)"
					+ "INNER JOIN TipoItem AS Ti1 ON (It1.idTipoItem = Ti1.idTipoItem)"
					+ "LEFT JOIN Item AS It2 ON (M.item2 = It2.idItem)"
					+ "INNER JOIN TipoItem AS Ti2 ON (It1.idTipoItem = Ti2.idTipoItem)"
					+ "LEFT JOIN Item AS It3 ON (M.item3 = It3.idItem)"
					+ "INNER JOIN TipoItem AS Ti3 ON (It1.idTipoItem = Ti3.idTipoItem)"
					+ "LEFT JOIN Item AS It4 ON (M.item4 = It4.idItem) "
					+ "INNER JOIN TipoItem AS Ti4 ON (It1.idTipoItem = Ti4.idTipoItem)"
					+ "LEFT JOIN Item AS It5 ON (M.item5 = It5.idItem) "
					+ "INNER JOIN TipoItem AS Ti5 ON (It1.idTipoItem = Ti5.idTipoItem)"
					+ "LEFT JOIN Item AS It6 ON (M.item6 = It6.idItem)"
					+ "INNER JOIN TipoItem AS Ti6 ON (It1.idTipoItem = Ti6.idTipoItem)"
					+ "LEFT JOIN Item AS It7 ON (M.item7 = It7.idItem) "
					+ "INNER JOIN TipoItem AS Ti7 ON (It1.idTipoItem = Ti7.idTipoItem)"
					+ "LEFT JOIN Item AS It8 ON (M.item8 = It8.idItem) "
					+ "INNER JOIN TipoItem AS Ti8 ON (It1.idTipoItem = Ti8.idTipoItem)"
					+ "LEFT JOIN Item AS It9 ON (M.item9 = It9.idItem) "
					+ "INNER JOIN TipoItem AS Ti9 ON (It1.idTipoItem = Ti9.idTipoItem)"
					+ "LEFT JOIN Item AS It10 ON (M.item10 = It10.idItem) "
					+ "INNER JOIN TipoItem AS Ti10 ON (It1.idTipoItem = Ti10.idTipoItem)"
					+ "LEFT JOIN Item AS It11 ON (M.item11 = It11.idItem) "
					+ "INNER JOIN TipoItem AS Ti11 ON (It1.idTipoItem = Ti11.idTipoItem)"
					+ "LEFT JOIN Item AS It12 ON (M.item12 = It12.idItem) "
					+ "INNER JOIN TipoItem AS Ti12 ON (It1.idTipoItem = Ti12.idTipoItem)"
					+ "LEFT JOIN Item AS It13 ON (M.item13 = It13.idItem) "
					+ "INNER JOIN TipoItem AS Ti13 ON (It1.idTipoItem = Ti13.idTipoItem)"
					+ "LEFT JOIN Item AS It14 ON (M.item14 = It14.idItem) "
					+ "INNER JOIN TipoItem AS Ti14 ON (It1.idTipoItem = Ti14.idTipoItem)"
					+ "LEFT JOIN Item AS It15 ON (M.item15 = It15.idItem) "
					+ "INNER JOIN TipoItem AS Ti15 ON (It1.idTipoItem = Ti15.idTipoItem)"
					+ "LEFT JOIN Item AS It16 ON (M.item16 = It16.idItem) "
					+ "INNER JOIN TipoItem AS Ti16 ON (It1.idTipoItem = Ti16.idTipoItem)"
					+ "LEFT JOIN Item AS It17 ON (M.item17 = It17.idItem) "
					+ "INNER JOIN TipoItem AS Ti17 ON (It1.idTipoItem = Ti17.idTipoItem)"
					+ "LEFT JOIN Item AS It18 ON (M.item18 = It18.idItem) "
					+ "INNER JOIN TipoItem AS Ti18 ON (It1.idTipoItem = Ti18.idTipoItem)"
					+ "LEFT JOIN Item AS It19 ON (M.item19 = It19.idItem)"
					+ "INNER JOIN TipoItem AS Ti19 ON (It1.idTipoItem = Ti19.idTipoItem)"
					+ "LEFT JOIN Item AS It20 ON (M.item20 = It20.idItem)"
					+ "INNER JOIN TipoItem AS Ti20 ON (It1.idTipoItem = Ti20.idTipoItem)" + "WHERE idMochila = ?");
			stSeleccionarMochila.setInt(1, idMochila);
			ResultSet result = stSeleccionarMochila.executeQuery();

			HashMap<Integer, PaqueteItem> items = new HashMap<Integer, PaqueteItem>();

			if (result.next()) {
				int indice = 22;
				for (int i = 1; i < 20; i++) {
					PaqueteItem item = new PaqueteItem();
					item.setId(result.getInt(indice));
					if (item.getId() > 0) {
						item.setNombre(result.getString(indice + 1));
						item.setIdTipo(result.getInt(indice + 2));
						item.setBonoAtaque(result.getInt(indice + 3));
						item.setTipo(result.getString(indice + 12));
						item.setBonoDefensa(result.getInt(indice + 4));
						item.setBonoMagia(result.getInt(indice + 5));
						item.setBonoSalud(result.getInt(indice + 6));
						item.setBonoEnergia(result.getInt(indice + 7));
						item.setFuerzaRequerida(result.getInt(indice + 8));
						item.setDestrezaRequerida(result.getInt(indice + 9));
						item.setInteligenciaRequerida(result.getInt(indice + 10));						
					}
					items.put(i, item);
					indice += 13; // Cantidad columnas por item
				}
			}
			return new PaqueteMochila(idMochila, items);
		} catch (

		SQLException ex) {
			Servidor.log.append("Fallo al intentar recuperar la mochila " + idMochila + System.lineSeparator());
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
			ex.printStackTrace();
		}

		return null;
	}

	public List<PaqueteItem> getItems() {
		try {
			PreparedStatement stSeleccionarItem = connect.prepareStatement(
					"SELECT * FROM Item AS It INNER JOIN TipoItem AS Ti ON (It.idTipoItem = Ti.idTipoItem) WHERE idItem <> -1");
			ResultSet result = stSeleccionarItem.executeQuery();

			List<PaqueteItem> items = new ArrayList<PaqueteItem>();
			while (result.next()) {
				PaqueteItem item = new PaqueteItem();
				item.setId(result.getInt(1));
				item.setNombre(result.getString(2));
				item.setIdTipo(result.getInt(3));
				item.setTipo(result.getString(13));
				item.setBonoAtaque(result.getInt(4));
				item.setBonoDefensa(result.getInt(5));
				item.setBonoMagia(result.getInt(6));
				item.setBonoSalud(result.getInt(7));
				item.setBonoEnergia(result.getInt(8));
				item.setFuerzaRequerida(result.getInt(9));
				item.setDestrezaRequerida(result.getInt(10));
				item.setInteligenciaRequerida(result.getInt(11));

				items.add(item);
			}
			return items;
		} catch (

		SQLException ex) {
			Servidor.log.append("Fallo al intentar recuperar los items ");
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
			ex.printStackTrace();
		}

		return null;
	}

	public PaqueteItem getItem(int idItem) {
		try {
			PreparedStatement stSeleccionarItem = connect.prepareStatement("SELECT * FROM Item AS It "
					+ "INNER JOIN TipoItem AS Ti ON (It.idTipoItem = Ti.idTipoItem)" + "WHERE idItem = ?");
			stSeleccionarItem.setInt(1, idItem);
			ResultSet result = stSeleccionarItem.executeQuery();

			PaqueteItem item = null;
			if (result.next()) {
				item = new PaqueteItem();
				item.setId(result.getInt(1));
				item.setNombre(result.getString(2));
				item.setTipo(result.getString(12));
				item.setBonoAtaque(result.getInt(3));
				item.setBonoDefensa(result.getInt(4));
				item.setBonoMagia(result.getInt(5));
				item.setBonoSalud(result.getInt(6));
				item.setBonoEnergia(result.getInt(7));
				item.setFuerzaRequerida(result.getInt(8));
				item.setDestrezaRequerida(result.getInt(9));
				item.setInteligenciaRequerida(result.getInt(10));
			}
			return item;
		} catch (

		SQLException ex) {
			Servidor.log.append("Fallo al intentar recuperar el item " + idItem + System.lineSeparator());
			Servidor.log.append(ex.getMessage() + System.lineSeparator());
			ex.printStackTrace();
		}

		return null;
	}

	public PaqueteUsuario getUsuario(String usuario) {
		ResultSet result = null;
		PreparedStatement st;

		try {
			st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
			st.setString(1, usuario);
			result = st.executeQuery();

			String password = result.getString("password");
			int idPersonaje = result.getInt("idPersonaje");

			PaqueteUsuario paqueteUsuario = new PaqueteUsuario();
			paqueteUsuario.setUsername(usuario);
			paqueteUsuario.setPassword(password);
			paqueteUsuario.setIdPj(idPersonaje);

			return paqueteUsuario;
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar recuperar el usuario " + usuario + System.lineSeparator());
			Servidor.log.append(e.getMessage() + System.lineSeparator());
			e.printStackTrace();
		}

		return new PaqueteUsuario();
	}
}
