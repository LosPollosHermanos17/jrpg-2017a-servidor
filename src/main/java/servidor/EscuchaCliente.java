package servidor;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import mensajeria.AdaptadorComando;
import mensajeria.Comando;

import mensajeria.ComandoConexiones;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

public class EscuchaCliente extends Thread {

	private final Socket socket;
	private final ObjectInputStream entrada;
	private final ObjectOutputStream salida;

	private Gson gson;
	private boolean desconectar;

	private PaquetePersonaje paquetePersonaje;
	private PaqueteUsuario paqueteUsuario;

	private Comando comando;

	public EscuchaCliente(Socket socket) throws IOException {
		this.socket = socket;
		this.salida = new ObjectOutputStream(socket.getOutputStream());
		this.entrada = new ObjectInputStream(socket.getInputStream());

		paquetePersonaje = new PaquetePersonaje();

		GsonBuilder gsonBilder = new GsonBuilder().registerTypeAdapter(Comando.class, new AdaptadorComando());
		this.gson = gsonBilder.create();
	}

	public void run() {
		try {
			while (!this.desconectar) {
				this.comando = this.recibirComando();
				this.comando.resolver(this);
			}
		} catch (IOException | ClassNotFoundException e) {
			Servidor.log.append("Error de conexion: " + e.getMessage() + System.lineSeparator());
			e.printStackTrace();
		}

		// Cierro las conexiones
		this.cerrarConexiones();

		// Elimino el cliente del servidor
		Servidor.getPersonajesConectados().remove(paquetePersonaje.getId());
		Servidor.getUbicacionPersonajes().remove(paquetePersonaje.getId());
		Servidor.getClientesConectados().remove(this);

		// Doy aviso a todos los clientes de la desconexión
		try {

			for (EscuchaCliente conectado : Servidor.getClientesConectados())
				conectado.enviarComando(new ComandoConexiones(Servidor.getPersonajesConectados()));
		} catch (IOException e) {
			Servidor.log.append("Error de conexion: " + e.getMessage() + System.lineSeparator());
			e.printStackTrace();
		}

		Servidor.log.append(this.comando.getIp() + " se ha desconectado." + System.lineSeparator());
	}

	public PaquetePersonaje getPaquetePersonaje() {
		return paquetePersonaje;
	}

	public void desconectar() {
		this.desconectar = true;
	}

	public void enviarComando(Comando comando) throws IOException {
		this.salida.writeObject(gson.toJson(comando, Comando.class));
	}

	public Comando recibirComando() throws JsonSyntaxException, ClassNotFoundException, IOException {
		return gson.fromJson((String) this.entrada.readObject(), Comando.class);
	}

	public void setPaqueteUsuario(PaqueteUsuario paqueteUsuario) {
		this.paqueteUsuario = paqueteUsuario;
	}

	public PaqueteUsuario getPaqueteUsuario() {
		return this.paqueteUsuario;
	}

	public void setPaquetePersonaje(PaquetePersonaje paquetePersonaje) {
		this.paquetePersonaje = paquetePersonaje;
	}

	public void cerrarConexiones() {
		try {
			this.entrada.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.salida.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Comando getComando() {
		return this.comando;
	}

	public String getHostAddress() {
		return this.socket.getInetAddress().getHostAddress();
	}
}
