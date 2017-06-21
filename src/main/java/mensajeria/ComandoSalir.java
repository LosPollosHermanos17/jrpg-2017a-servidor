package mensajeria;

import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoSalir extends Comando implements Serializable {

	@Override
	public void resolver(EscuchaCliente cliente) {
		// Cierro las conexiones
		cliente.cerrarConexiones();
		// Lo elimino de los clientes conectados
		Servidor.getClientesConectados().remove(cliente);
		// Indico que se desconecto
		Servidor.log.append(cliente.getComando().getIp() + " se ha desconectado." + System.lineSeparator());
	}
}