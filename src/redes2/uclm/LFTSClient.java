package redes2.uclm;

import java.io.*;
import java.net.*;
import java.util.InputMismatchException;
import java.util.Scanner;

import javax.net.ssl.*;

public class LFTSClient {
	public static void main(String[] args) {
		// Comprobamos la entrada de argumentos
		/*
		 * if (args.length < 4 && args.length > 4) { System.out.
		 * println("Uso: java LFTSClient modo=<modo> host=<host> puerto=<puerto> carpeta_cliente=<carpeta_cliente>"
		 * ); return; } else if (args.length < 3 && args.length > 3 ) { System.out.
		 * println("Uso: java LFTSClient host=<host> puerto=<puerto> carpeta_cliente=<carpeta_cliente>"
		 * ); return; } else if (args.length < 2) {
		 * System.out.println("Uso: java LFTSClient host=<host> puerto=<puerto> ");
		 * return; }
		 */
		// Obtener los valores de los parametros
		String accion = "";
		String modo = ""; // ¿usa SSL?
		String serverAddress = ""; // Dirección IP del servidor
		int serverPort = 8000; // Puerto en el que el servidor escucha las conexiones
		String carpetaCliente = "";
		for (String arg : args)
			if (arg.startsWith("modo="))
				modo = arg.substring(5);
			else if (arg.startsWith("hosts="))
				serverAddress = arg.substring(5);
			else if (arg.startsWith("puerto="))
				serverPort = Integer.parseInt(arg.substring(7));
			else if (arg.startsWith("carpeta_cliente="))
				carpetaCliente = arg.substring(16);

		try {
			Socket socket = null;
			SSLSocket socketSSL = null;
			DataInputStream in = null;
			DataOutputStream out = null;
			if (!modo.equals("SSL")) {
				System.out.println("Conexión SSL no implementada");
				// Conexión usando sockets comunes
				socket = new Socket(serverAddress, serverPort);
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
			} else {
				System.out.println("Conexión SSL implementada");
				// Configurar la conexión SSL
				SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				socketSSL = (SSLSocket) socketFactory.createSocket(serverAddress, serverPort);
				socketSSL.startHandshake();
				in = new DataInputStream(socketSSL.getInputStream());
				out = new DataOutputStream(socketSSL.getOutputStream());
			}
			String[] aciones = { "LIST", "PUT", "GET", "FIN" };
			Scanner sc = new Scanner(System.in);
			do {
				for (int i = 0; i < aciones.length; i++)
					System.out.println(i + ". " + aciones[i]);
				try {
					accion = aciones[sc.nextInt()];
				} catch (InputMismatchException | ArrayIndexOutOfBoundsException e) {
					sc.nextLine();
					continue;
				}
				// Enviar una petición al servidor
				try {
					String nombreArchivo = "audio.AAC";
					if (accion.equals("LIST"))
						Listar(in, out);
					if (accion.equals("PUT"))
						Subir(out, carpetaCliente, sc);
					if (accion.equals("GET"))
						Bajar(in, out, carpetaCliente, sc);
				} catch (IOException e) {
					ClientHandler.registrarError(e.getMessage());
				}
			} while (!accion.equals("FIN"));
			System.out.println("Adios");
			// Cerrar conexiones
			sc.close();
			in.close();
			out.close();
			socket.close();
			if (socketSSL != null)
				socketSSL.close();
		} catch (IOException e) {
			ClientHandler.registrarError(e.getMessage());
		}
	}

	private static void Listar(DataInputStream in, DataOutputStream out) throws IOException {
		out.writeUTF("LIST"); // Petición de listar archivos en el servidor
		// Leer la respuesta del servidor
		String mensaje = in.readUTF();
		String[] cadenas = mensaje.split("\n");
		for (int i = 0; i < cadenas.length; i++)
			System.out.println(cadenas[i]);
	}

	private static void Subir(DataOutputStream out, String carpetaCliente, Scanner sc) throws IOException {
		System.out.println("Introduce el nombre del archivo con su extension");
		String nombreArchivo = sc.next();
		String direccion = carpetaCliente + "\\" + nombreArchivo;
		File file = new File(direccion);
		if (file.exists() && !file.isDirectory()) {
			out.writeUTF("PUT" + nombreArchivo);
			FileInputStream fileInputStream = new FileInputStream(file);
			out.writeLong(file.length());
			byte[] buffer = new byte[4 * 1024];
			int bytes = 0;
			while ((bytes = fileInputStream.read(buffer)) != -1) {
				out.write(buffer, 0, bytes);
				out.flush();
			}
			fileInputStream.close();
		} else if (!file.exists())
			System.out.println("El archivo no existe");
	}

	private static void Bajar(DataInputStream in, DataOutputStream out, String carpetaCliente, Scanner sc)
			throws IOException {
		System.out.println("Introduce el nombre del archivo con su extension");
		String nombreArchivo = sc.next();
		sc.nextLine();
		out.writeUTF("GET" + nombreArchivo);
		int bytes = 0;
		FileOutputStream fileOutputStream = new FileOutputStream(carpetaCliente + "\\" + nombreArchivo);
		long size = in.readLong();

		byte[] buffer = new byte[4 * 1024];
		while (size > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
			fileOutputStream.write(buffer, 0, bytes);
			size -= bytes;
		}
		if (size == -1)
			System.out.println("El archivo no existe");
		else
			System.out.println("Archivo recibido");
		fileOutputStream.close();
	}
}
