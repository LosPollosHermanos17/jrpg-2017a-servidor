package mensajeria;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoSalirMercado extends Comando implements Serializable {
	
	private int idPersonaje;

	@Override
	public void resolver(EscuchaCliente cliente) {

		// Guardo nuevo estado del personaje en el servidor
		PaquetePersonaje personajeSaliente = Servidor.getPersonajesConectados().get(this.idPersonaje);
		personajeSaliente.setComerciando(false);
		
		// Seteo el estado de todos sus items como no ofertados
		Map<Integer, PaqueteItem> itemsPersonajeSaliente = personajeSaliente.getPaqueteInventario().getItems();		
		for (Entry<Integer, PaqueteItem> entry : itemsPersonajeSaliente.entrySet())
			entry.getValue().setOfertado(false);

		// Le aviso a todos los demás clientes que este usuario salió del mercado
		for (EscuchaCliente clienteConectado : Servidor.getClientesConectados()) {
			try {
				// Evito enviarme el comando a mi mismo
				if (clienteConectado.getId() != cliente.getId())
					clienteConectado.enviarComando(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
