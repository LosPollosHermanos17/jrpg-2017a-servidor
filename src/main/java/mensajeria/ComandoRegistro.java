package mensajeria;

import java.io.IOException;
import java.io.Serializable;

import servidor.Servidor;
import servidor.EscuchaCliente;

public class ComandoRegistro extends Comando implements Serializable {

	private PaqueteUsuario paqueteUsuario;

	@Override
	public void resolver(EscuchaCliente cliente) {

		// Si el usuario se pudo registrar le envio un msj de exito
		if (Servidor.getConector().registrarUsuario(this.paqueteUsuario))
			this.setMensaje(Comando.msjExito);
		// Si el usuario no se pudo registrar le envio un msj de fracaso
		else
			this.setMensaje(Comando.msjFracaso);

		// Le contesto al cliente el resultado
		try {			
			cliente.setPaqueteUsuario(this.paqueteUsuario);
			cliente.enviarComando(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
