package mensajeria;

import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoConexion extends Comando implements Serializable {

	private PaquetePersonaje paquetePersonaje;

	@Override
	public void resolver(EscuchaCliente cliente) {
		// Guardo el paquete con estado nuevo
		cliente.setPaquetePersonaje(paquetePersonaje);
		
		// Actualizo los personajes y ubicaciones
		Servidor.getPersonajesConectados().put(paquetePersonaje.getId(), paquetePersonaje);
		Servidor.getUbicacionPersonajes().put(paquetePersonaje.getId(),	new PaqueteMovimiento(paquetePersonaje.getId()));

		// Doy aviso a todo del nuevo estado de este personaje
		synchronized (Servidor.atencionConexiones) {
			Servidor.atencionConexiones.notify();
		}
	}
}