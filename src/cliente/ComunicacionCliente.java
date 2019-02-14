package cliente;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ComunicacionCliente {
	public final static int PUERTO = 8080;
	public final static String IP_DEFAULT = "127.0.0.1";

	private Socket s;

	private BufferedReader cIn;
	private BufferedWriter cOut;

	private DataInputStream sIn;
	private DataOutputStream sOut;
	private String ip;

	/**
	 * Constructor
	 */
	public ComunicacionCliente() {
		cIn = new BufferedReader(new InputStreamReader(System.in));
		cOut = new BufferedWriter(new OutputStreamWriter(System.out));
		crearComunicacion();
		leerConsola();
	}

	public static void main(String[] args) {
		new ComunicacionCliente();
	}

	
	/**
	 * Inicializa la comunicación con el servidor y crea el hilo que espera los mensajes.
	 */
	public void crearComunicacion() {
		try {
			cOut.write("Ingrese la IP de destino \n(presione Enter si el servidor es local, :i: para información) \n");
			cOut.flush();
			// Inicializa la comunicación con el servidor
			ip = nextLine();
			if (ip.equals("") || !ip.contains("."))
				ip = IP_DEFAULT;
			s = new Socket(ip, PUERTO);
			sOut = new DataOutputStream(s.getOutputStream());
			sIn = new DataInputStream(s.getInputStream());
			
			// Ciclo para obtener información desde el servidor
			new Thread() {
				public void run() {
					try {
						while (true) {
							recibirMensajes();
							Thread.sleep(500);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ciclo que lee lo que se escriba en la consola mientras la aplicación corre.
	 */
	public void leerConsola() {	
		new Thread() {
			public void run() {
				try {
					escribirConsola("Escribe un mensaje:\n");
					while (true) {
						String line = nextLine();
						enviarMensaje(line);
						sleep(500);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
		

	}

	/**
	 * Lee una línea desde la consola
	 * 
	 * @return String obtenido de la Consola
	 * @throws IOException
	 */
	public String nextLine() throws IOException {
		return cIn.readLine();
	}

	/**
	 * Lee una línea obteinda del servidoe
	 * 
	 * @return String recibido del servidor
	 * @throws IOException
	 */
	public String nextLineServer() throws IOException {
		return sIn.readUTF();
	}

	/**
	 * Recibe un mensaje del servidor y lo escribe en la consola.
	 * 
	 * @throws IOException
	 */
	public void recibirMensajes() throws IOException {
		String mensaje = nextLineServer();
		escribirConsola(mensaje + "\n");
	}

	/**
	 * Escribe un mensaje en la consola
	 * 
	 * @param mensaje
	 *            Mensaje a mostrar en la consola
	 * @throws IOException
	 */
	public void escribirConsola(String mensaje) throws IOException {
		cOut.write(mensaje);
		cOut.flush();
	}

	/**
	 * Envía un mensaje al servidor
	 * 
	 * @param mensaje
	 *            Mensaje a enviar
	 * @throws IOException
	 */
	public void enviarMensaje(String mensaje) throws IOException {
		sOut.writeUTF(mensaje);
		sOut.flush();
	}

}
