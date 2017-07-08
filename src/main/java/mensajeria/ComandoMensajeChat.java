package mensajeria;

import java.io.IOException;
import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoMensajeChat extends Comando implements Serializable {

	private String desde;
	private String para;
	private String mensajeChat;
	private boolean esPrivado;

	@Override
	public void resolver(EscuchaCliente cliente) {
		try {
			if (this.esPrivado) {
				// Si el cliente está conectado
				//if (Servidor.getPersonajesConectados().containsKey(this.para)) {
					// Obtengo el cliente
					for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
						if (conectado.getPaquetePersonaje().getNombre().equals(this.para)) {							
							// Doy aviso al cliente
							conectado.enviarComando(this);
						}
					}
					// Logueo
					Servidor.log.append("Mensaje de " + this.desde + " para " + this.para + " : " + this.mensajeChat);
					
					//Seteo que el comando fue realizado correctamente
					this.mensaje = this.msjExito;
					
					// Le devuelvo el mensaje al que lo envió
					cliente.enviarComando(this);
				//}
			} else {
				// Recorro todos los clientes conectados
				for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
					// Seteo que el comando fue realizado correctamente
					this.mensaje = this.msjExito;
					// Doy aviso al cliente
					conectado.enviarComando(this);
					// Logueo
					Servidor.log.append("Mensaje de " + this.desde + " para TODOS: " + this.mensajeChat);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
