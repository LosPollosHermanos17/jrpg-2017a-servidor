package mensajeria;

import java.io.IOException;
import java.io.Serializable;

import estados.Estado;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoFinalizarBatalla extends Comando implements Serializable {

	private int id;
	private int idEnemigo;

	@Override
	public void resolver(EscuchaCliente cliente) {
		try {
			Servidor.getPersonajesConectados().get(this.id).setEstado(Estado.estadoJuego);
			Servidor.getPersonajesConectados().get(this.idEnemigo).setEstado(Estado.estadoJuego);
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				if (conectado.getPaquetePersonaje().getId() == this.idEnemigo)
					conectado.enviarComando(this);
			}

			synchronized (Servidor.atencionConexiones) {
				Servidor.atencionConexiones.notify();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
