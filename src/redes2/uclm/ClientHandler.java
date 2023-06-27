package redes2.uclm;

import java.io.*;
import java.net.*;
import java.util.Calendar;

public class ClientHandler implements Runnable {
	private Socket cliente;
	private String carpetaServidor;

	public ClientHandler(Socket cliente, String carpetaServidor) {
		this.cliente = cliente;
		this.carpetaServidor = carpetaServidor;

	}

	@Override
	public void run() {

		manejador(cliente, carpetaServidor);
	}

	public static void manejador(Socket cliente, String carpetaServidor) {
		DataInputStream in = null;
		DataOutputStream out = null;
		try {
			// Obtener los flujos de entrada y salida del cliente
			in = new DataInputStream(cliente.getInputStream());
			out = new DataOutputStream(cliente.getOutputStream());
			// Leer la petición del cliente
			String opcion = in.readUTF();
			if (opcion.trim().equals("LIST"))
				// Envíar la lista de archivos en la carpeta del servidor
				listar(out, carpetaServidor);
			else if (opcion.trim().startsWith("PUT"))
				guardar(in, carpetaServidor, opcion.substring(3));
			else if (opcion.trim().startsWith("GET"))
				dar(out, carpetaServidor, opcion.substring(3));
			else
				// Petición inválida
				System.out.println("Petición inválida");
			// Cerrar conexiones
			in.close();
			out.close();
			cliente.close();
			System.out.println("Cliente desconectado: " + cliente.getInetAddress().getHostAddress());
			registrarAccion("Cliente desconectado: " + cliente.getInetAddress().getHostAddress());
		} catch (IOException e) {
			e.printStackTrace();
			registrarError("Error al manejar la conexión con el cliente: " + e.getMessage());
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				registrarError(e.getMessage());
			}

		}
	}

	private static void listar(DataOutputStream out, String carpetaServidor) throws IOException {
		File folder = new File(carpetaServidor);
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			StringBuilder response = new StringBuilder();
			for (File file : files)
				response.append(file.getName()).append(" (").append(file.length()).append(" bytes)\n");
			out.writeUTF(response.toString());
			registrarAccion("Contenido de la carpeta "+carpetaServidor+" enviado");
		} else if (!folder.exists()) {
			out.writeUTF("NO existe la carpeta");
			registrarError("NO existe la carpeta");
		}else
			out.writeUTF("NO es una carpeta");
	}

	private static void guardar(DataInputStream in, String carpetaServidor, String nombreArchivo) throws IOException {
		int bytes = 0;
		FileOutputStream fileOutputStream = new FileOutputStream(carpetaServidor + "\\" + nombreArchivo);
		long size = 0;
		try {
			size = in.readLong(); // read file size
		} catch (EOFException e) {
			registrarError("El clente no manda la longitud del archivo");
			fileOutputStream.close();
			return;
		}
		byte[] buffer = new byte[4 * 1024];
		while (size > 0 && (bytes = in.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
			fileOutputStream.write(buffer, 0, bytes);
			size -= bytes; 
		}
		System.out.println("Archivo guardado");
		registrarAccion("Archivo guardado");
		fileOutputStream.close();
	}

	private static void dar(DataOutputStream out, String carpetaServidor, String nombreArchivo) throws IOException {
		String direccion = carpetaServidor + "\\" + nombreArchivo;
		File file = new File(direccion);
		if (file.exists()) {
			FileInputStream fileInputStream = new FileInputStream(file);
			out.writeLong(file.length());
			byte[] buffer = new byte[4 * 1024];
			int bytes = 0;
			while ((bytes = fileInputStream.read(buffer)) != -1) {
				out.write(buffer, 0, bytes);
				out.flush();
			}
			fileInputStream.close();
			registrarAccion("Fichero enviado");
		}
		else
			out.writeLong(-1);
	}
	
	public static void registrarAccion(String accion) {
		try {
			FileWriter archivo = new FileWriter(new File(".\\bin\\acciones.log"), true);
			archivo.write(devolverFecha()
					+ " -> " + accion + "\n");
			archivo.close();
		} catch (IOException e) {
			registrarError(e.getMessage());
		}
	}

	public static void registrarError(String mensaje) {
		try {
			FileWriter archivo = new FileWriter(new File("bin\\errores.log"), true);
			archivo.write(devolverFecha()
					+ " -> " + mensaje + "\n");
			archivo.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private static String devolverFecha() {
		Calendar fechaActual = Calendar.getInstance();
		return (String.valueOf(fechaActual.get(Calendar.DAY_OF_MONTH)) + "/"
				+ String.valueOf(fechaActual.get(Calendar.MONTH) + 1) + "/"
				+ String.valueOf(fechaActual.get(Calendar.YEAR)) + "; "
				+ String.valueOf(fechaActual.get(Calendar.HOUR_OF_DAY)) + ":"
				+ String.valueOf(fechaActual.get(Calendar.MINUTE)) + ":"+ String.valueOf(fechaActual.get(Calendar.SECOND)));
	}

}
