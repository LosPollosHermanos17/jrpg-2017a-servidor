package mensajeria;

import java.io.Serializable;

import servidor.EscuchaCliente;

public class ComandoDesconectar extends Comando implements Serializable {

	@Override
	public void resolver(EscuchaCliente cliente) {
		cliente.desconectar();
	}
}
