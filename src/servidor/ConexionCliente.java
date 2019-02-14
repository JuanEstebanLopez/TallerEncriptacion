package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConexionCliente extends Thread {

	public final static String COMMAND = ":";
	public final int HASH = 2;

	public final static String COMMAND_ME = "me";
	public final static String COMMAND_U = "users";
	public final static String COMMAND_I = "i";

	private ComunicacionServidor principal;
	private Socket s;
	private String nombre;
	private boolean conectado;

	private DataInputStream sIn;
	private DataOutputStream sOut;

	/**
	 * Constructor
	 * 
	 * @param principal
	 *            Referencia a la clase principal
	 * @param s
	 *            Socket con la conexión del cliente
	 */
	public ConexionCliente(ComunicacionServidor principal, Socket s) {
		this.principal = principal;
		this.s = s;
		this.conectado = true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			sOut = new DataOutputStream(s.getOutputStream());
			sIn = new DataInputStream(s.getInputStream());
			// Recibe mensajes del cliente mientras el usuario está conectado
			while (conectado) {
				recibirMensajes();
				sleep(500);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recibe y maneja los mensajes del cliente.
	 * 
	 * @throws IOException
	 */
	public void recibirMensajes() throws IOException {
		// Recibe el mensaje del cliente en un string
		String mensaje = sIn.readUTF();
		if (!mensaje.startsWith(COMMAND)) {
			// si no es un comando, devuelve el mensaje encriptado
			enviarMensaje(encriptarMensaje(mensaje, HASH));
		} else {
			if (mensaje.startsWith(COMMAND + COMMAND)) { // Comando para desencriptar mensaje
				// el mensaje para desencriptar puede o no estar separado por espacio
				if (mensaje.startsWith(":: "))
					enviarMensaje(encriptarMensaje(mensaje.split(" ", 2)[1], -HASH));
				else
					enviarMensaje(encriptarMensaje(mensaje.replaceAll("::", ""), -HASH));
			} else { // si no es para desencriptar, maneja el comando
				manageCommand(mensaje.split(" ", 2));
			}
		}
	}

	/**
	 * Maneja un comando recibido desde un cliente
	 * 
	 * @param info
	 *            [0]: comando, [1]: mensaje
	 * @throws IOException
	 */
	public void manageCommand(String[] info) throws IOException {
		String command = info[0].replaceAll(":", "");
		String data = info.length >= 2 ? info[1] : "";

		switch (command) {
		case COMMAND_I: // Caso de informacion
			enviarMensaje(":: mensaje -> Desencriptar mensaje\n:me: -> Consultar nombre\n:me: nuevoNombre -> Actualizar nombre\n:users: -> Ver usuarios\n:nombreUsuario: mensaje -> enviar mensaje encriptado al usuario\n:i: -> Ver comandos");
			break;
		case COMMAND_U: // pedir lista de usuario
			enviarMensaje("Usuatios conectados: \n" + principal.UsuariosConectados());
			break;
		case COMMAND_ME: // consultar o actualizar nombre
			if (!"".equals(data))
				if (principal.agregarUsuario(this, data))
					setNombre(data);
				else
					enviarMensaje("No se puede Asignar el nombre \"" + command + "\"");
			else
				enviarMensaje("Tu nombre de usuario es: " + nombre);
			break;

		default: // enviar mensaje a un usuario
			data = encriptarMensaje(data, HASH);
			if (principal.enviarMensaje(command, nombre + ": " + data))
				enviarMensaje("enviado a " + command + ": " + data);
			break;
		}
	}

	/**
	 * Envía un mensaje al cliente
	 * 
	 * @param mensaje
	 *            mensaje a enviar
	 * @throws IOException
	 */
	public void enviarMensaje(String mensaje) throws IOException {
		sOut.writeUTF(mensaje);
		sOut.flush();
	}

	/**
	 * Encripta o desencripta un mensaje
	 * 
	 * @param men
	 *            mensaje a procesar
	 * @param hash
	 *            Has para encriptar si es positivo, para desencriptar si es
	 *            negativo
	 * @return mensaje procesado
	 */
	public String encriptarMensaje(String men, int hash) {
		char[] en = men.toCharArray();
		for (int i = 0; i < en.length; i++) {
			en[i] += hash;
		}
		return new String(en);
	}

	/**
	 * Actualiza el nombre e informa al cliente
	 * 
	 * @param nombre
	 *            Nuevo nombre del cliente
	 */
	public void setNombre(String nombre) {
		this.nombre = nombre;
		try {
			enviarMensaje("Su usuario es: " + nombre);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return Nombre del cliente
	 */
	public String getNombre() {
		return nombre;
	}

}
