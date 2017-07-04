package mensajeria;

import java.io.IOException;
import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoConsultarItemsOfertados extends Comando implements Serializable {
	
	private int idPersonajeSeleccionado;
	private PaquetePersonaje personajeActualizado;

	@Override
	public void resolver(EscuchaCliente cliente) {
		
		// Obtengo los datos del personaje seleccionado y se los devuelvo al cliente
		// Los datos del personaje seleccionado (mas precisamente el estado de sus
		// items (ofertados/no ofertados) se encuentran actualizados en el servidor
		
		personajeActualizado = Servidor.getPersonajesConectados().get(this.idPersonajeSeleccionado);
		
		try {
			cliente.enviarComando(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}

}
