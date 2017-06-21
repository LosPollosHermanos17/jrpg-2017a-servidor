package mensajeria;

import java.io.IOException;
import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoCrearPersonaje extends Comando implements Serializable {

	private PaquetePersonaje paquetePersonaje;

	@Override
	public void resolver(EscuchaCliente cliente) {		
		// Guardo el personaje en ese usuario
		Servidor.getConector().registrarPersonaje(paquetePersonaje, cliente.getPaqueteUsuario());

		// Le envio el id del personaje
		try {
			cliente.enviarComando(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}