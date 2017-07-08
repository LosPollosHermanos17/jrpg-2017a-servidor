package mensajeria;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoOfertarItem extends Comando implements Serializable {
	
	private int idPersonaje;
	private int idItem;
	private PaquetePersonaje personajeActualizado;

	@Override
	public void resolver(EscuchaCliente cliente) {
		
		// La key de itemsInventario hace referencia a la posicion dentro del mismo, tengo que recorrer
		// las 6 posiciones del inventario y preguntar en cada una si es el item al cual le tengo que cambiar el estado
		Map<Integer, PaqueteItem> itemsInventario = Servidor.getPersonajesConectados().get(this.idPersonaje).getPaqueteInventario().getItems();
		
		for (Entry<Integer, PaqueteItem> entry : itemsInventario.entrySet()) {
			
			if (entry.getValue().getId() == this.idItem)
				entry.getValue().setOfertado(true);
		}
		
		// Devuelvo mi personaje actualizado a todos los demas clientes
		personajeActualizado = Servidor.getPersonajesConectados().get(this.idPersonaje);
		
		// Tengo que mandar un comando a todos los clientes y si hay alguno que justo me tenga seleccionado
		// entonces llamar al comando consultarItemsOfertados automaticamente para actualizar la grilla
		for (EscuchaCliente clienteConectado : Servidor.getClientesConectados()) {
			try {
				// Evito enviar el comando a mi mismo
				if (clienteConectado.getPaquetePersonaje().getId() != cliente.getPaquetePersonaje().getId())
					clienteConectado.enviarComando(this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
