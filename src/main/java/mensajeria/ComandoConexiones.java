package mensajeria;

import java.io.Serializable;
import java.util.Map;

import servidor.EscuchaCliente;

public class ComandoConexiones extends Comando implements Serializable {

	private Map<Integer, PaquetePersonaje> personajes;

	public ComandoConexiones(Map<Integer, PaquetePersonaje> personajes) {
		this.personajes = personajes;
	}

	@Override
	public void resolver(EscuchaCliente cliente) {
	}
}