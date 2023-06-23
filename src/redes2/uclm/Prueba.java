package redes2.uclm;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Prueba {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int valor=0;
		String[]aciones= {"LIST","PUT","GET"};
		String as="";
		while (as.equals(""))
			try {
				System.out.println("afd");
				valor=sc.nextInt();
				as= aciones[valor];
				System.out.println(valor);
			} catch (InputMismatchException |ArrayIndexOutOfBoundsException e) {
				sc.nextLine();
				continue;
			}
		System.out.println(valor);
		System.out.println(as);
	}
}
