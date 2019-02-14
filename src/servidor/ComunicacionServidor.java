package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ComunicacionServidor {
	public final int PUERTO = 8080;
	public final String BASE_USUARIO_KEY = "user";

	private ServerSocket ss;

	HashMap<String, ConexionCliente> usuarios;

	/**
	 * Constructor
	 */
	public ComunicacionServidor() {
		usuarios = new HashMap<String, ConexionCliente>();
	}

	public static void main(String[] args) {
		(new ComunicacionServidor()).aceptarClientes();

	}

	/**
	 * Inicializa el servidor y acepta usuarios en un ciclo que dura mientras la aplicación corre.
	 */
	public void aceptarClientes() {
		try {
			System.out.println("Esperando usuarios ...");
			ss = new ServerSocket(PUERTO);

			new Thread() {
				@Override
				public void run() {
					while (true) {
						try {
							Socket s = ss.accept();
							ConexionCliente con = new ConexionCliente(ComunicacionServidor.this, s);
							con.start();
							sleep(500);
							con.setNombre(agregarUsuario(con));
							System.out.println("Usuario agregado: " + con.getNombre());
							sleep(500);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				}
			}.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Registra un nuevo usuario a la apicación.
	 * @param con Conexión con el usuario que se registrará
	 * @return Nombre del usuario registrado
	 */
	public String agregarUsuario(ConexionCliente con) {
		int num = 1;
		while (usuarios.containsKey(BASE_USUARIO_KEY + num)) {
			num++;
		}
		String k = BASE_USUARIO_KEY + num;
		usuarios.put(k, con);
		return k;
	}

	/**
	 * Registra un usuario existente con un nuevo nombre
	 * @param con Conexión con el usuario que se registrará
	 * @param nombre Nombre con el que se registrará
	 * @return true si se pudo asignar, false en caso contrario
	 */
	public boolean agregarUsuario(ConexionCliente con, String nombre) {
		if (!usuarios.containsKey(nombre)) {
			usuarios.put(nombre, con);
			return true;
		}
		return false;
	}
	
	/**
	 * Envía un mensaje a un usuario registrado
	 * @param to Nombre con el que el usuario está registrado
	 * @param data Mensaje a enviar
	 * @return true si el usuario existe, false en caso contrario
	 * @throws IOException
	 */
	public boolean enviarMensaje(String to, String data) throws IOException {
		if(usuarios.containsKey(to)) {
			usuarios.get(to).enviarMensaje(data);
			return true;
		}
			return false;
		
	}
	
	/**
	 * @return Lista con los nombres registrados de los usuarios
	 */
	public String UsuariosConectados() {
		StringBuilder b = new StringBuilder();
		for(String n : usuarios.keySet()){
			b.append(n+"\n");
		}
		return b.toString();
	}

}
