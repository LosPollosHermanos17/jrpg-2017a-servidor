package mensajeria;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import estados.Estado;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ComandoBatalla extends Comando implements Serializable, Cloneable {

	private int id;
	private int idEnemigo;
	private boolean miTurno;
	private List<PaqueteItem> items;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdEnemigo() {
		return idEnemigo;
	}

	public void setIdEnemigo(int idEnemigo) {
		this.idEnemigo = idEnemigo;
	}

	public boolean isMiTurno() {
		return miTurno;
	}

	public void setMiTurno(boolean miTurno) {
		this.miTurno = miTurno;
	}

	public List<PaqueteItem> getItems() {
		return this.items;
	}

	public void setItems(List<PaqueteItem> items) {
		this.items = items;
	}


	@Override
	public void resolver(EscuchaCliente cliente) {
		try {
			// Le reenvio al id del personaje batallado que quieren pelear
			Servidor.log.append(this.getId() + " quiere batallar con " + this.getIdEnemigo() + System.lineSeparator());

			// Cargo los items para dar sortear de la batalla
			this.setItems(Servidor.getConector().getItems());

			// Seteo estado de batalla
			Servidor.getPersonajesConectados().get(this.getId()).setEstado(Estado.estadoBatalla);
			Servidor.getPersonajesConectados().get(this.getIdEnemigo()).setEstado(Estado.estadoBatalla);
			this.setMiTurno(true);
		
			// Envío el comando
			cliente.enviarComando(this);			
			
			// Envío comando al enemigo para cambia estado
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				if (conectado.getPaquetePersonaje().getId() == this.getIdEnemigo()) {
					// Creo una copia del comando
					ComandoBatalla comando = (ComandoBatalla)super.clone();
					
					// Invierto los roles
					int aux = comando.getId();
					comando.setId(comando.getIdEnemigo());
					comando.setIdEnemigo(aux);
					comando.setMiTurno(false);					
					
					// Envío el nuevo comando al cliente enemigo
					conectado.enviarComando(comando);
				}
			}
			
			// Actualizo el nuevo estado de los jugadores en los otros clientes
			synchronized (Servidor.atencionConexiones) {
				Servidor.atencionConexiones.notify();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
