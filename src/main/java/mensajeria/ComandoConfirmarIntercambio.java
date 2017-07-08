package mensajeria;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import dominio.Item;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoConfirmarIntercambio extends Comando implements Serializable {
	
	private int idPersonajeSolicitante;
	private int idPersonajeSolicitado;
	private int idItemPersonajeSolicitante;
	private int idItemPersonajeSolicitado;
	
	@Override
	public void resolver(EscuchaCliente cliente) {
		
		// Aca tengo que efectuar el intercambio
		// Qué necesito?
		// idPersonajeSolicitante
		// idPersonajeSolicitado
		// idItemPersonajeSolicitante
		// idItemPersonajeSolicitado
		
		// Tengo que preguntar por el valor de los id de los items. Si estan en cero significa que el
		// usuario que recibio la solicitud, clickeo el boton Cancelar.
		if (idItemPersonajeSolicitante != 0 && idItemPersonajeSolicitado != 0) {
			
		
			PaquetePersonaje personajeSolicitante = Servidor.getPersonajesConectados().get(idPersonajeSolicitante);
			PaquetePersonaje personajeSolicitado = Servidor.getPersonajesConectados().get(idPersonajeSolicitado);		
			
			// Busco el item del personaje solicitante
			PaqueteItem itemPersonajeSolicitante = null;
			Map<Integer, PaqueteItem> itemsPersonajeSolicitante = personajeSolicitante.getPaqueteInventario().getItems();
			for (Entry<Integer, PaqueteItem> entry : itemsPersonajeSolicitante.entrySet()) {
				if (entry.getValue().getId() == idItemPersonajeSolicitante)
					itemPersonajeSolicitante = entry.getValue();
			}
			
			// Buso el item del personaje solicitado
			PaqueteItem itemPersonajeSolicitado = null;
			Map<Integer, PaqueteItem> itemsPersonajeSolicitado = personajeSolicitado.getPaqueteInventario().getItems();
			for (Entry<Integer, PaqueteItem> entry : itemsPersonajeSolicitado.entrySet()) {
				if (entry.getValue().getId() == idItemPersonajeSolicitado)
					itemPersonajeSolicitado = entry.getValue();
			}
			
			
			// Borro el item del personaje solicitante
			personajeSolicitante.getPaqueteInventario().getItems().put(itemPersonajeSolicitante.getIdTipo(), new PaqueteItem(-1));
			
			// Borro el item del personaje solicitado
			personajeSolicitado.getPaqueteInventario().getItems().put(itemPersonajeSolicitado.getIdTipo(), new PaqueteItem(-1));
			
			
			// Asigno el nuevo item al personaje solicitante
			personajeSolicitante.getPaqueteInventario().getInventario().agregarItem(itemPersonajeSolicitado.getItem());
			
			// Asigno el nuevo item al personaje solicitado
			personajeSolicitante.getPaqueteInventario().getInventario().agregarItem(itemPersonajeSolicitante.getItem());
	
		}
		
		// Tengo que avisar a todos los usuarios que se completo el intercambio
		// Los personajes participantes deben desbloquear la lista
		// Los otros personajes deben volver a agregarlos
		for (EscuchaCliente clienteConectado : Servidor.getClientesConectados()) {
			try {
					clienteConectado.enviarComando(this);
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}

	}

}
