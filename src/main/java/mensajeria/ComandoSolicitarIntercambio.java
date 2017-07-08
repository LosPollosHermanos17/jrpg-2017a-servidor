package mensajeria;

import java.io.IOException;
import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoSolicitarIntercambio extends Comando implements Serializable {

	private int idPersonajeSolicitante;
	private int idPersonajeSolicitado;
	private int idItemSolicitado;

	@Override
	public void resolver(EscuchaCliente cliente) {

		for (EscuchaCliente clienteConectado : Servidor.getClientesConectados()) {
			try {
				// Envio el comando a todos los personajes
				// Si es el personaje solicitado, se le mostrara la opcion para
				// realizar el intercambio.
				// Si es alguno de los otros personajes, se bloquearan los
				// personajes solicitante/solicitado en la lista.
				// Tengo que evitar enviar el comando a mi mismo.
				if (clienteConectado.getPaquetePersonaje().getId() != cliente.getPaquetePersonaje().getId())
					clienteConectado.enviarComando(this);
			} catch (IOException e) {
				Servidor.log.append("Error recibiendo solicitud de intercambio en marcado: " + e.toString());
			}
		}
	}
}
