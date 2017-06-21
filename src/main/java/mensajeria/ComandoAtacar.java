package mensajeria;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoAtacar extends Comando implements Serializable, Cloneable {

	private HashMap<String, Integer> atributosPersonaje = new HashMap<String, Integer>();
	private HashMap<String, Integer> atributosEnemigo = new HashMap<String, Integer>();

	@Override
	public void resolver(EscuchaCliente cliente) {
		try {
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				if (conectado.getPaquetePersonaje().getId() == this.atributosEnemigo.get("id"))
					conectado.enviarComando(this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
