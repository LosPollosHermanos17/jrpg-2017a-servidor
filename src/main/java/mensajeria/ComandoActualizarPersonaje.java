package mensajeria;

import java.io.IOException;
import java.io.Serializable;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoActualizarPersonaje extends Comando implements Serializable {

	private PaquetePersonaje paquetePersonaje;

	@Override
	public void resolver(EscuchaCliente cliente) {
		Servidor.getConector().actualizarPersonaje(this.paquetePersonaje);		
		Servidor.getPersonajesConectados().remove(this.paquetePersonaje.getId());
		Servidor.getPersonajesConectados().put(this.paquetePersonaje.getId(), this.paquetePersonaje);

		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			try {
				conectado.enviarComando(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
