package mensajeria;

import java.io.Serializable;

import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoMovimiento extends Comando implements Serializable {

	private PaqueteMovimiento paqueteMovimiento;

	@Override
	public void resolver(EscuchaCliente cliente) {
		
		Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setPosX(paqueteMovimiento.getPosX());
		Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setPosY(paqueteMovimiento.getPosY());
		Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setDireccion(paqueteMovimiento.getDireccion());
		Servidor.getUbicacionPersonajes().get(paqueteMovimiento.getIdPersonaje()).setFrame(paqueteMovimiento.getFrame());
		
		synchronized(Servidor.atencionMovimientos){
			Servidor.atencionMovimientos.notify();
		}
	}
}