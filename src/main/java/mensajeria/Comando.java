package mensajeria;

import java.io.Serializable;

import servidor.EscuchaCliente;
public abstract class Comando implements Serializable, Cloneable {
	
	public static String msjExito = "1";
	public static String msjFracaso = "0";
	
	private String mensaje;
	private String ip;

	public Comando() {
		
	}
	
	public Comando(String mensaje, String nick, String ip, int comando) {
		this.mensaje = mensaje;
		this.ip = ip;	
	}
	
	public Comando(String mensaje, int comando) {
		this.mensaje = mensaje;	
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMensaje() {
		return mensaje;
	}


	public String getIp() {
		return ip;
	}
	
	public abstract void resolver(EscuchaCliente cliente);
	
	public Object clone() {
		Object obj = null;
		try {
			obj = super.clone();
		} catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
		}
		return obj;
	}

}
