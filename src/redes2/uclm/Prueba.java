package redes2.uclm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Prueba {
	public static void main(String[] args) {
		/*String javaPathKeyStore = "C:/Users/yo/certificados/clientKey.jks";
        KeyStore keyStore = null;//Almacén de claves
		try {
			keyStore = KeyStore.getInstance("JKS");
		} catch (KeyStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//Tipo de almacén

        try {
			keyStore.load(new FileInputStream(javaPathKeyStore), "clientpass".toCharArray());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		ClientHandler.registrarAccion("casaa");
		System.out.println("algo");
	}
}
