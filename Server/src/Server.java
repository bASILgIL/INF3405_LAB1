import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Server {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Adresse IP : ");
		String adresseIp = sc.nextLine();
		String zeroTo255 = "(\\d{1,2}|(0|1)\\" + "d{2}|2[0-4]\\d|25[0-5])";  
		String regex = zeroTo255 + "\\."+ zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;   
		while(!adresseIp.matches(regex)) {
			System.out.println("L'adresse n'est pas valide. Recommencer");
			adresseIp = sc.nextLine();
		}
		System.out.println("Port : ");
		String port;
		while(true) {
		try {
			port = sc.nextLine();
			if(Integer.parseInt(port) <= 5050 && Integer.parseInt(port) >= 5000) {
				break;
			}
			System.out.println("Veuillez rentrer un port entre 5000 et 5050 : ");
		}catch(NumberFormatException e) {
			System.out.println("Veuillez rentrer un chiffre :");
		}
		}

		
//		
//		String host = args.length < 0 ? args[0] : "localhost";
//		
//		for (int i = 5000; i < 5050; i++) {
//			
//			try (Socket s = new Socket(host, i)){
//				
//				System.out.println("There is a server on port " + i + " of " + host);
//				
//			} catch (UnknownHostException e) {
//				
//				System.err.println(e);
//				break;
//				
//			} catch (IOException e) {
//				
//			}
//		}
	}

}
