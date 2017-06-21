package mensajeria;

import java.io.Serializable;
import java.util.Map;

import cliente.EscuchaMensajes;
import servidor.EscuchaCliente;

public class ComandoMovimientos extends Comando implements Serializable {

	private Map<Integer, PaqueteMovimiento> movimientos;

	public ComandoMovimientos(Map<Integer, PaqueteMovimiento> movimientos) {
		this.movimientos = movimientos;
	}

	@Override
	public void resolver(EscuchaCliente cliente) {	
	}
}