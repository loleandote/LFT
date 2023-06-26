package redes2.uclm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class LFTServer {

	private int puerto;
	private String carpetaServidor;
	private int maxClientes;

	public LFTServer(int puerto, String carpetaServidor, int maxClientes) {
		this.puerto = puerto;
		this.carpetaServidor = carpetaServidor;
		this.maxClientes = maxClientes;
	}

	public void iniciarServidor() {
		ServerSocket servidor = null;

		try {
			// Iniciamos el servidor en el puerto especificado
			servidor = new ServerSocket(puerto);
			System.out.println("LFTServer iniciado en el puerto " + puerto);
			ClientHandler.registrarAccion("LFTServer iniciado en el puerto " + puerto);
			// Crear el ExecutorService para administrar los hilos de los clientes
			ExecutorService executor = Executors.newFixedThreadPool(maxClientes);
			while (true) {
				// Esperamos a que un cliente se conecte
				Socket cliente = servidor.accept();
				System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
				ClientHandler.registrarAccion("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
				if (maxClientes > 0)
					executor.execute(() -> ClientHandler.manejador(cliente, carpetaServidor));
				else
					new ClientHandler(cliente, carpetaServidor).run();
			}
		} catch (IOException e) {
			ClientHandler.registrarError("Error al iniciar el servidor: " + e.getMessage());
		} finally {
			if (servidor != null) {
				try {
					servidor.close();
				} catch (IOException e) {
					ClientHandler.registrarError("Error al cerrar el servidor: " + e.getMessage());
				}
			}
		}
	}

	private SSLServerSocket configurarSSL() {
		String java_path = "./certificados/serverkey.jks";
		SSLServerSocket serverSocket = null;
		// Conseguir una factoría de sockets y un ServerSocket
		try {
			// Acceso al almacén de claves
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream(java_path), "servpass".toCharArray());
			// Acceso a las claves del almacén
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, "servpass".toCharArray());
			KeyManager[] keyManagers = kmf.getKeyManagers();
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(keyManagers, null, null);
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			serverSocket = (SSLServerSocket) ssf.createServerSocket(puerto);
			System.out.println("servidor arrancado...");
			ClientHandler.registrarAccion("servidor arrancado...");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return serverSocket;
	}

	private void iniciarServidorSSL() {
		SSLServerSocket socket=null;
		try {
			socket=configurarSSL();
			// Iniciamos el servidor en el puerto especificado
			System.out.println("LFTServer iniciado en el puerto " + puerto + " en modo seguro");

			// Crear el ExecutorService para administrar los hilos de los clientes
			ExecutorService executor = Executors.newFixedThreadPool(maxClientes);
			while (true) {
				// Esperamos a que un cliente se conecte
				Socket cliente = socket.accept();
				System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
				ClientHandler.registrarAccion("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
				if (maxClientes > 0)
					executor.execute(() -> ClientHandler.manejador(cliente, carpetaServidor));
				else
					new ClientHandler(cliente, carpetaServidor).run();
			}
		} catch (IOException e) {
			ClientHandler.registrarError("Error al iniciar el servidor: " + e.getMessage());
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					ClientHandler.registrarError("Error al cerrar el servidor: " + e.getMessage());
				}
			}
		}
	}

	public static void main(String[] args) {
		// Comprobamos la entrada de argumentos
		/*
		 * if (args.length < 2) {
		 * System.out.println("Ej. Uso: java LFTSServer <modo> <puerto> "); return; }
		 * else if (args.length < 3) { System.out.
		 * println("Ej. Uso: java LFTSServer <modo> <puerto> <carpeta_cliente>");
		 * return; } else if (args.length != 4) { System.out.
		 * println("Ej. Uso: java LFTSServer <modo> <puerto> <carpeta_cliente> <max_clientes>"
		 * ); return; }
		 */

		String modo = "";
		int puerto = -1;
		String carpetaServidor = "";
		int maxClientes = 0;

		for (String arg : args) {
			if (arg.startsWith("modo=")) {
				modo = arg.substring(5);
			} else if (arg.startsWith("puerto=")) {
				puerto = Integer.parseInt(arg.substring(7));
			} else if (arg.startsWith("carpeta_servidor=")) {
				carpetaServidor = arg.substring(17);
			} else if (arg.startsWith("max_clientes=")) {
				maxClientes = Integer.parseInt(arg.substring(13));
			}
		}

		// Creamos una instancia del servidor y lo iniciamos
		LFTServer servidor = new LFTServer(puerto, carpetaServidor, maxClientes);
		if(puerto >0)
		if (modo.equals(""))
			servidor.iniciarServidor();
		else {
			servidor.iniciarServidorSSL();
		}
	}

}
