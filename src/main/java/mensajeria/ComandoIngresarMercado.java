package mensajeria;

import java.io.IOException;
import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoIngresarMercado extends Comando implements Serializable {

	private int idPersonaje;

	@Override
	public void resolver(EscuchaCliente cliente) {

		// Guardo el estado Comerciando en el personaje
		Servidor.getPersonajesConectados().get(this.idPersonaje).setComerciando(true);

		// Le aviso a todos los demás clientes que este usuario ingresó al mercado
		for (EscuchaCliente clienteConectado : Servidor.getClientesConectados()) {
			try {
				clienteConectado.enviarComando(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
