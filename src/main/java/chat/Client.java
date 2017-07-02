package chat;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;

import mensajeria.Command;
import mensajeria.Packet;
import mensajeria.PacketLogout;
import mensajeria.PacketMessage;
import mensajeria.PacketUpdate;
import mensajeria.PacketUser;

public class Client extends Thread {
	private UIServer uiServer;
	private Server server;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Gson gson;
	private Packet packet;
	private PacketUser user;

	public Client(UIServer uiServer, Server server, Socket socket) {
		this.uiServer = uiServer;
		this.server = server;
		this.socket = socket;
		this.gson = new Gson();
	}

	/*
	 * Escucha mensajes provenientes desde cliente y realiza las acciones
	 * pertinentes
	 */
	public void run() {

		try {
			this.in = new ObjectInputStream(this.socket.getInputStream());
			this.out = new ObjectOutputStream(this.socket.getOutputStream());

			while (packet == null || packet.getCommand() != Command.LOGOUT) {
				// Bloqueo leyendo lo que hay en el socket
				String readedObject = (String) this.in.readObject();

				// Deserializo la info a la clase padre Packet
				packet = this.gson.fromJson(readedObject, Packet.class);

				// Verifico que comando fue enviado desde el cliente
				switch (packet.getCommand()) {
				case LOGIN: {
					PacketUser packetUser = gson.fromJson(readedObject, PacketUser.class);
					// Guardo el usuario
					this.user = packetUser;
					// Valido si es correcto el usuario y contraseña.
					if (this.validateUser(packetUser)) {

						// Seteo al usuario como logueado
						this.user.setLogged(true);

						// Genero paquete de tipo LOGIN para la actualizacion de
						// usuarios
						List<String> users = new LinkedList<String>(this.server.getClients().keySet());
						packet = new PacketUpdate(users);
						packet.setCommand(Command.LOGIN);

						// Seteo al usuario local como logueado
						packet.setStatus(true);
						this.uiServer.log("Usuario " + this.user.getUsername() + " logueado correctamente.");
					} else {
						packet.setStatus(false);
						this.uiServer.log("Usuario o passwrod incorrecto. Usuario: " + packetUser.getUsername()
								+ " Password:" + packetUser.getPassword());
					}

					// Envio el paquete al cliente
					this.out.writeObject(this.gson.toJson(packet));

					// Si el usuario se logueo
					if (this.user.getLogged()) {
						// Agrego el cliente a la lista de conectados
						this.server.getClients().put(this.user.getUsername(), this);
						// Envio lista de clientes conectados
						this.updateAllClients();
					}
					break;
				}
				case LOGOUT: {
					// Deslogueo usuario
					this.user.setLogged(false);
					// Confirmo el comando
					this.packet.setStatus(true);
					// Reenvio comando
					this.out.writeObject(this.gson.toJson(this.packet, Packet.class));
					// Logueo
					this.uiServer.log("Usuario " + this.user.getUsername() + " deslogueado.");
					break;
				}
				case MESSAGE: {
					PacketMessage packetMessage = gson.fromJson(readedObject, PacketMessage.class);

					if (packetMessage.isPrivate()) {
						// Si el cliente está conectado
						if (this.server.getClients().containsKey(packetMessage.getTo())) {
							// Obtengo el cliente
							Client client = this.server.getClients().get(packetMessage.getTo());
							// Le envio el mensaje
							client.sendMessage(packetMessage.getFrom(), packetMessage.getTo(),
									packetMessage.getMessage(), packetMessage.isPrivate());
							// Logueo
							this.uiServer.log("Mensaje de " + packetMessage.getFrom() + " para " + packetMessage.getTo()
									+ " : " + packetMessage.getMessage());
							// Seteo que el comando fue realizado correctamente
							packetMessage.setStatus(true);
							// Doy aviso al cliente
							this.out.writeObject(gson.toJson(packetMessage, PacketMessage.class));
						}
					} else {
						// Recorro todos los clientes conectados
						for (Client client : this.server.getClients().values()) {
							// Le envio el mensaje a todos
							client.sendMessage(packetMessage.getFrom(), packetMessage.getTo(),
									packetMessage.getMessage(), packetMessage.isPrivate());
							// Logueo
							this.uiServer.log("Mensaje de " + packetMessage.getFrom() + " para TODOS: "
									+ packetMessage.getMessage());
						}
					}

					break;
				}
				default:
					break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			this.uiServer.log("Exception en Cliente. " + e.toString());
		}

		// Elimino el cliente de cliente conectado
		this.server.getClients().remove(this.user.getUsername());

		// Envio lista de clientes conectados
		this.updateAllClients();

		// Logueo en el server la desconexion
		this.uiServer.log("Cliente " + this.user.getUsername() + " desconectado.");
	}

	private boolean validateUser(PacketUser packetUser) {
		if (this.server.getUsers().containsKey(packetUser.getUsername())) {
			String password = this.server.getUsers().get(packetUser.getUsername());
			return packetUser.getPassword().equals(password);
		}
		return false;
	}

	/*
	 * Devuelve el usuario de este cliente
	 */
	public PacketUser getUser() {
		return this.user;
	}

	/*
	 * Envia al cliente la lista de usuarios conectados
	 */
	public void updateClients() {
		try {
			List<String> users = new LinkedList<String>(this.server.getClients().keySet());
			this.out.writeObject(this.gson.toJson(new PacketUpdate(users)));
		} catch (IOException e) {
			this.uiServer.log(e.toString());
		}
	}

	/*
	 * Envia a todos los clientes la lista de usuarios conectados
	 */
	private void updateAllClients() {
		for (Client client : this.server.getClients().values()) {
			client.updateClients();
		}
	}

	/*
	 * Envia un mensae a un determinado cliente
	 */
	public void sendMessage(String from, String to, String message, boolean isPrivate) {
		try {
			PacketMessage packetMessage = new PacketMessage(from, to, message, isPrivate);
			this.out.writeObject(this.gson.toJson(packetMessage));
		} catch (IOException e) {
			this.uiServer.log(e.toString());
		}
	}

	/*
	 * Detiene el cliente
	 */
	public void close() {
		if (this.socket != null) {
			try {
				this.socket.close();
			} catch (IOException e) {
				this.socket = null;
			}
		}

	}
}
