package mensajeria;

import java.io.IOException;
import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoInicioSesion extends Comando implements Serializable {

	private PaqueteUsuario paqueteUsuario;
	private PaquetePersonaje paquetePersonaje;

	@Override
	public void resolver(EscuchaCliente cliente) {	
		// Si se puede loguear el usuario le envio un mensaje de exito y el paquete personaje con los datos
		if (Servidor.getConector().loguearUsuario(this.paqueteUsuario)) {
			
			// OBtengo el personaje con el usuario
			this.paquetePersonaje = Servidor.getConector().getPersonaje(this.paqueteUsuario);
			
			// Guardo el personaje
			cliente.setPaquetePersonaje(this.paquetePersonaje);
			
			// Indico que el comando fue exitoso
			this.setMensaje(Comando.msjExito);
			
		} else {
			this.setMensaje(Comando.msjFracaso);			
		}
		
		try {
			// Envío la respuesta al cliente
			cliente.enviarComando(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
