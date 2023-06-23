package redes2.uclm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LFTServer {

	private String modo;
	private int puerto;
	private String carpetaServidor;
	private int maxClientes;

	public LFTServer(String modo, int puerto, String carpetaServidor, int maxClientes) {
		this.modo = modo;
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

			// Crear el ExecutorService para administrar los hilos de los clientes
			ExecutorService executor = Executors.newFixedThreadPool(maxClientes);
			while (true) {
				// Esperamos a que un cliente se conecte
				Socket cliente = servidor.accept();
				System.out.println("Cliente conectado: " + cliente.getInetAddress().getHostAddress());
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
		int puerto = 8000;
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
		LFTServer servidor = new LFTServer(modo, puerto, carpetaServidor, maxClientes);
		servidor.iniciarServidor();
	}

	
}
