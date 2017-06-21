package servidor;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.ComandoMovimientos;

public class AtencionMovimientos extends Thread {

	public void run() {

		synchronized (this) {
			try {
				while (true) {
					// Espero a que se conecte alguien
					wait();
					try {
						// Le reenvio la conexion a todos
						for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
							if (conectado.getPaquetePersonaje().getEstado() == Estado.estadoJuego)
								conectado.enviarComando(new ComandoMovimientos(Servidor.getUbicacionPersonajes()));
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