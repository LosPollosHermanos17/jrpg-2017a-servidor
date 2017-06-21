package mensajeria;

import java.io.Serializable;

import servidor.Servidor;
import servidor.EscuchaCliente;

public class ComandoMostrarMapa extends Comando implements Serializable {

	private PaquetePersonaje paquetePersonaje;

	@Override
	public void resolver(EscuchaCliente cliente) {
		Servidor.log.append(cliente.getHostAddress() + " ha elegido el mapa "
				+ paquetePersonaje.getMapa() + System.lineSeparator());
	}
}
