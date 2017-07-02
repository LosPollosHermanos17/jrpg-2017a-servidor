package chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class Server implements Runnable {
	private ServerSocket serverSocket;
	private Thread thread = null;
	private Map<String, Client> clients;
	private UIServer uiServer;
	private Map<String, String> users;

	public Server(UIServer uiServer, int port) throws IOException {
		this.uiServer = uiServer;
		this.serverSocket = new ServerSocket(port);
		clients = new HashMap<String, Client>();

		this.users = new HashMap<String, String>();
		try {
			Scanner s = new Scanner(new File("TPChat/server/users.txt"));
			while (s.hasNext())
				this.users.put(s.next(), s.next());
			s.close();
		} catch (FileNotFoundException e) {
			this.uiServer.log("Ha ocurrido un error al leer el archivo de usuarios.");
			throw e;
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
			this.uiServer.log("Servidor iniciado.");
		}
	}

	public void stop() {
		if (thread != null) {
			thread = null;
			this.closeClients();
			this.closeServer();
			this.uiServer.log("Servidor detenido.");
		}
	}

	@Override
	public void run() {

		while (thread != null) {
			try {
				this.uiServer.log("Esperando a un cliente...");
				Socket socketClient = this.serverSocket.accept();
				this.uiServer.log("Cliente conectado: " + socketClient);
				for (Client c : this.clients.values())
					this.uiServer.log("Clientes conectados: " + c.getUser().getUsername());
				Client client = new Client(this.uiServer, this, socketClient);
				client.start();
			} catch (Exception ie) {
				this.uiServer.log("Excepción en el servidor: " + ie.toString());
			}
		}
	}

	private void closeClients() {
		for (Client client : this.clients.values())
			client.close();
	}

	private void closeServer() {
		if (this.serverSocket != null) {
			try {
				this.serverSocket.close();
			} catch (IOException e) {
				this.serverSocket = null;
			}
		}
	}

	public Map<String, Client> getClients() {
		return this.clients;
	}
	
	public Map<String, String> getUsers() {
		return this.users;
	}

}
