import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	private static Scanner sc = new Scanner(System.in);
	private static Socket socket = null;
	private static DataInputStream input = null;
	private static DataOutputStream output = null;

	public static void main(String[] args) throws Exception {
		
		// Adresse et port du serveur
		
		String serverAddress = getIpAddress();
		int serverPort = getPort();
		
		// Nom d'utilisateur et mot de passe
		
		String userName = getUser();
		String password = getPassword();
		
		// try/catch inspiré de : https://www.geeksforgeeks.org/socket-programming-in-java/
		
		try {
			// Création d'une connection
			
			socket = new Socket(serverAddress,serverPort);
			System.out.format("Le serveur fonctionne sur %s:%d%n", serverAddress,serverPort);
			
			// Création d'un canal pour recevoir les messages du serveur
			
			input = new DataInputStream(socket.getInputStream());
			
			// Création d'un canal pour envoyer des messages au serveur 
			
			output = new DataOutputStream(socket.getOutputStream());
		}
	    catch(UnknownHostException u) {
	    	System.out.println(u);
	    }
	    catch(IOException i) {
	        System.out.println(i);
	    }
		
		// Envoie du nom et mot de passe de l'utilisateur
		
		output.writeUTF(userName);
		output.writeUTF(password);
		
		// Réponse de validation du nom et mot de passe de l'utilisateur du côté serveur
		
		String serverAnswer = input.readUTF();
		// TODO : success and failure response from server should be in a file with constants
		if (serverAnswer == "USER_PASSWORD-NOT_FOUND") {
			System.out.println("Erreur dans la saisie du mot de passe");
			closeConnection();
			return;
		}
		
		
		
		socket.close();
	}
	
	private static void closeConnection() {
		try {
			input.close();
			output.close();
			socket.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}  
	}

	
	private static String getUser() {
		return getInfo(
				"Veuillez entrer votre nom d'utilisateur : ", 
				"Le nom d'utilisateur %s est invalide. Veuillez entrer un nom d'utilisateur qui n'est pas vide: "
				);
	}
	
	private static String getPassword() {
		return getInfo(
				"Veuillez entrer votre mot de passe : ", 
				"Le mot de passe %s est invalide. Veuillez entrer un mot de passe qui n'est pas vide: "
				);
	}
	
	private static String getInfo(String demandMessage, String warningMessage) {
		System.out.println(demandMessage);
		String userInfo;
		
		while (true) {
			userInfo = sc.nextLine();
			if (userInfo != null && !userInfo.isEmpty() && !userInfo.trim().isEmpty()) {
				return userInfo;
			}
			System.out.format(warningMessage, userInfo);
		}
	}

	private static int getPort() {
		int port = 0;
		System.out.println("Veuillez entrer un port entre 5000 et 5050 inclusivement: ");
		
		while (true) {
			try {
				port = Integer.parseInt(sc.nextLine());
				if (port <= 5050 && port >= 5000) {
					return port;
				}
				System.out.format("Le port %d est invalide. Veuillez entrer un port entre 5000 et 5050 inclusivement: ", port);
			} 
			catch (NumberFormatException e) {
				System.out.format("Le port %d est invalide. Le port Veuillez entrer un chiffre :", port);
			}
		}
	}

	private static String getIpAddress() {
		String zeroTo255 = "(\\d{1,2}|(0|1)\\" + "d{2}|2[0-4]\\d|25[0-5])";
		String regex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
		
		System.out.println("Veuillez entrer une adresse IP : ");
		String IPAddress = sc.nextLine();
		
		while (!IPAddress.matches(regex)) {
			System.out.format("L'adresse IP %s n'est pas valide. Recommencer", IPAddress);
			IPAddress = sc.nextLine();
		}
		
		return IPAddress;
	}

}
