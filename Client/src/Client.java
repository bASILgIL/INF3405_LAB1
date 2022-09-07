import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	private static Scanner sc = new Scanner(System.in);
	private static Socket socket;

	public static void main(String[] args) throws Exception {
		String serverAddress = getIpAddress();
		int serverPort = getPort();
		String user = getUser();
		getPassword();
		socket = new Socket(serverAddress,serverPort);
		System.out.format("The server is running %s:%d%n", serverAddress,serverPort);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.writeUTF(user);
		
		socket.close();
	}

	private static void getPassword() {
		
	}

	private static String getUser() {
		//TODO
		return null;
	}

	private static int getPort() {
		System.out.println("Port : ");
		int port;
		while (true) {
			try {
				port = Integer.parseInt(sc.nextLine());
				if (port <= 5050 && port >= 5000) {
					return port;
				}
				System.out.println("Veuillez rentrer un port entre 5000 et 5050 : ");
			} catch (NumberFormatException e) {
				System.out.println("Veuillez rentrer un chiffre :");
			}
		}
	}

	private static String getIpAddress() {
		System.out.println("Adresse IP : ");
		String IPAddress = sc.nextLine();
		String zeroTo255 = "(\\d{1,2}|(0|1)\\" + "d{2}|2[0-4]\\d|25[0-5])";
		String regex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
		while (!IPAddress.matches(regex)) {
			System.out.println("L'adresse n'est pas valide. Recommencer");
			IPAddress = sc.nextLine();
		}
		return IPAddress;
	}

}
