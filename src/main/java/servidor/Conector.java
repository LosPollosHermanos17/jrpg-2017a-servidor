package servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mensajeria.Paquete;
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
			;
		} catch (SQLException e) {
			Servidor.log.append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
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

			int idInventario = result.getInt("idInventario");
			int idMochila = result.getInt("idMochila");

			// Selecciono los datos del inventario
			PreparedStatement stSeleccionarInventario = connect.prepareStatement(
					"SELECT * FROM Inventario AS INV "
					+ "LEFT JOIN Item AS It1 ON (INV.manos1 = It1.idItem) "
					+ "LEFT JOIN Item AS It2 ON (INV.manos2 = It2.idItem) "
					+ "LEFT JOIN Item AS It3 ON (INV.pie = It3.idItem) "
					+ "LEFT JOIN Item AS It4 ON (INV.cabeza = It4.idItem) "
					+ "LEFT JOIN Item AS It5 ON (INV.pecho = It5.idItem) "
					+ "LEFT JOIN Item AS It6 ON (INV.accesorio = It6.idItem) "
					+ "WHERE idInventario = ?");
			stSeleccionarInventario.setInt(1, idInventario);
			result = stSeleccionarInventario.executeQuery();
			
			Servidor.log.append("Id: " + result.getInt(1));
			Servidor.log.append(" 1: " + result.getInt(2));
			Servidor.log.append(" 2: " + result.getInt(3));
			Servidor.log.append(" 3: " + result.getInt(4));
			Servidor.log.append(" 4: " + result.getInt(5));
			Servidor.log.append(" 5: " + result.getInt(6));
			Servidor.log.append(" 6: " + result.getInt(7));
			
			Map<Integer, PaqueteItem> items = new HashMap<Integer, PaqueteItem>();
			int indice = 8;
			Servidor.log.append("Item? " + result.getString(indice));
			for (int i = 0; i < 6; i++) {
				PaqueteItem item = new PaqueteItem();
				Servidor.log.append("Indice: " + result.getInt(indice));
				if (result.getInt(indice) > 0) {
					item.setId(result.getInt(indice));
					item.setBonoAtaque(result.getInt(indice + 1));
					item.setBonoDefensa(result.getInt(indice + 2));
					item.setBonoMagia(result.getInt(indice + 3));
					item.setBonoSalud(result.getInt(indice + 4));
					item.setBonoEnergia(result.getInt(indice + 5));
					item.setFuerzaRequerida(result.getInt(indice + 6));
					item.setDestrezaRequerida(result.getInt(indice + 7));
					item.setInteligenciaRequerida(result.getInt(indice + 8));

					items.put(item.getId(), item);
				}
				indice += 9;
			}
			PaqueteInventario inventario = new PaqueteInventario(idInventario, items);
			personaje.setInventario(inventario);

			// Selecciono los datos de la Mochila
			PreparedStatement stSeleccionarMochila = connect.prepareStatement(
					"SELECT * FROM Mochila AS M "
					+ "LEFT JOIN Item AS It1 ON (M.item1 = It1.idItem) "
					+ "LEFT JOIN Item AS It2 ON (M.item2 = It2.idItem) "
					+ "LEFT JOIN Item AS It3 ON (M.item3 = It3.idItem) "
					+ "LEFT JOIN Item AS It4 ON (M.item4 = It4.idItem) "
					+ "LEFT JOIN Item AS It5 ON (M.item5 = It5.idItem) "
					+ "LEFT JOIN Item AS It6 ON (M.item6 = It6.idItem) "
					+ "LEFT JOIN Item AS It7 ON (M.item7 = It7.idItem) "
					+ "LEFT JOIN Item AS It8 ON (M.item8 = It8.idItem) "
					+ "LEFT JOIN Item AS It9 ON (M.item9 = It9.idItem) "
					+ "LEFT JOIN Item AS It10 ON (M.item10 = It10.idItem) "
					+ "LEFT JOIN Item AS It11 ON (M.item11 = It11.idItem) "
					+ "LEFT JOIN Item AS It12 ON (M.item12 = It12.idItem) "
					+ "LEFT JOIN Item AS It13 ON (M.item13 = It13.idItem) "
					+ "LEFT JOIN Item AS It14 ON (M.item14 = It14.idItem) "
					+ "LEFT JOIN Item AS It15 ON (M.item15 = It15.idItem) "
					+ "LEFT JOIN Item AS It16 ON (M.item16 = It16.idItem) "
					+ "LEFT JOIN Item AS It17 ON (M.item17 = It17.idItem) "
					+ "LEFT JOIN Item AS It18 ON (M.item18 = It18.idItem) "
					+ "LEFT JOIN Item AS It19 ON (M.item19 = It19.idItem) "
					+ "LEFT JOIN Item AS It20 ON (M.item20 = It20.idItem) "
					+ "WHERE idMochila = ?");
			stSeleccionarMochila.setInt(1, idMochila);
			result = stSeleccionarMochila.executeQuery();

			items = new HashMap<Integer, PaqueteItem>();

			indice = 22;
			for (int i = 0; i < 20; i++) {
				PaqueteItem item = new PaqueteItem();
				if (result.getInt(indice) > 0) {
					item.setId(result.getInt(indice++));
					item.setBonoAtaque(result.getInt(indice + 1));
					item.setBonoDefensa(result.getInt(indice + 2));
					item.setBonoMagia(result.getInt(indice + 3));
					item.setBonoSalud(result.getInt(indice + 4));
					item.setBonoEnergia(result.getInt(indice + 5));
					item.setFuerzaRequerida(result.getInt(indice + 6));
					item.setDestrezaRequerida(result.getInt(indice + 7));
					item.setInteligenciaRequerida(result.getInt(indice + 8));

					items.put(item.getId(), item);
				}
				indice += 9;
			}
			PaqueteMochila mochila = new PaqueteMochila(idMochila, items);
			personaje.setMochila(mochila);

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
