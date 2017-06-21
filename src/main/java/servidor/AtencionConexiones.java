package servidor;

import estados.Estado;
import mensajeria.ComandoConexiones;

public class AtencionConexiones extends Thread {

	public void run() {

		synchronized (this) {
			try {
				while (true) {
					// Espero a que se conecte alguien
					wait();
					try {
						// Le reenvio la conexion a todos
						for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
							if (conectado.getPaquetePersonaje().getEstado() != Estado.estadoOffline)
								conectado.enviarComando(new ComandoConexiones(Servidor.getPersonajesConectados()));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (InterruptedException e1) {				
				e1.printStackTrace();
			}
		}
	}
}