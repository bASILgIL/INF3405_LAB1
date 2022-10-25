
import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Client {
	private static final Scanner sc = new Scanner(System.in);
	private static Socket socket;
	private static DataInputStream input;
	private static DataOutputStream output;

	public static void main(String[] args) throws IOException {
		
		// Adresse et port du serveur
		String serverAddress = getIpAddress();
		int serverPort = getPort();
		
		// Création d'une connection
		socket = new Socket(serverAddress,serverPort);
		System.out.printf("Le serveur fonctionne sur %s:%d%n\n", serverAddress,serverPort);
			
		// Création d'un canal pour recevoir les messages du serveur
		InputStream in = socket.getInputStream();
		input = new DataInputStream(new BufferedInputStream(in));

		// Création d'un canal pour envoyer des messages au serveur
		OutputStream out = socket.getOutputStream();
		output = new DataOutputStream(out);

		// Nom d'utilisateur et mot de passe
		String userName = getUser();
		String password = getPassword();
		
		// Envoie du nom et mot de passe de l'utilisateur
		writeString(userName);
		writeString(password);
		
		// Validation du nom et mot de passe de l'utilisateur du côté serveur
		boolean isUserInfoValid = input.readBoolean();
		if (!isUserInfoValid) {
			System.out.println("Erreur dans la saisie du mot de passe");
			closeConnection();
			return;
		}
		System.out.println("La connection a été établie.\n");
	
		// Envoie de l'image à traiter
		sendImageToProcess();
		String newImageName = getNewImageName();
		writeString(newImageName);
		System.out.printf("L'image \"%s\" a été envoyée pour être traitée.\n", newImageName);
		
		// Reception de l'image traitée
		// Inspiré de : https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
		BufferedImage processedImage = readImage();

		// Envoie de la confirmation de réception de l'image traitée
		output.writeBoolean(true);
       
		// Enregistrement de l'image traitée
		saveImage(newImageName, processedImage);
		
		// Confirmation de réception de l'image traitée
		System.out.printf("L'image \"%s\" a été traitée.  Elle se trouve ici : Client/images/%s.jpg", newImageName, newImageName);
		
		// Fermeture de la connection
		closeConnection();
	}
	
	private static String getPathImageToProcess() {
		return getInfo("Veuillez entrer le nom (chemin d'accès) de l'image à traiter : ", 
				"Le nom d'image %s est invalide. Veuillez entrer un nom qui n'est pas vide : ");
	}
	
	private static String getNewImageName() {
		return getInfo("Comment voulez-vous nommer l'image traitée? ", 
				"Le nom d'image %s est invalide. Veuillez entrer un nom qui n'est pas vide : ");
	}
	
	private static String getUser() {
		return getInfo(
				"Veuillez entrer votre nom d'utilisateur : ", 
				"Le nom d'utilisateur %s est invalide. Veuillez entrer un nom d'utilisateur qui n'est pas vide : \n"
				);
	}
	
	private static String getPassword() {
		return getInfo(
				"Veuillez entrer votre mot de passe : ", 
				"Le mot de passe %s est invalide. Veuillez entrer un mot de passe qui n'est pas vide : \n"
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
			System.out.printf(warningMessage, userInfo);
		}
	}

	private static int getPort() {
		int port = 0;
		System.out.println("Veuillez entrer un port entre 5000 et 5050 inclusivement : ");
		
		while (true) {
			try {
				port = Integer.parseInt(sc.nextLine());
				if (port <= 5050 && port >= 5000) {
					return port;
				}
				System.out.printf("Le port %d est invalide. Veuillez entrer un port entre 5000 et 5050 inclusivement : \n", port);
			} catch (NumberFormatException e) {
				System.out.printf("Le port %d est invalide. Le port Veuillez entrer un chiffre :\n", port);
			}
		}
	}

	private static String getIpAddress() {
		String zeroTo255 = "(\\d{1,2}|(0|1)\\" + "d{2}|2[0-4]\\d|25[0-5])";
		String regex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
		
		System.out.println("Veuillez entrer une adresse IP : ");
		String IPAddress = sc.nextLine();
		
		while (!IPAddress.matches(regex)) {
			System.out.printf("L'adresse IP %s n'est pas valide. Veuillez recommencer\n", IPAddress);
			IPAddress = sc.nextLine();
		}
		
		return IPAddress;
	}

	private static BufferedImage readImage() throws IOException {
		int size = input.readInt();
		byte[] imageAr = new byte[size];
		input.readFully(imageAr);
		return ImageIO.read(new ByteArrayInputStream(imageAr));
	}
	
	private static void saveImage(String imageName, BufferedImage processedImage) throws IOException {
		File outputFile = new File("Client/images/"+ imageName +".jpg");
		ImageIO.write(processedImage, "jpg", outputFile);
	}
	
	private static void sendImageToProcess() {
		String imagePath;
		boolean isImageValid = false;
		while (!isImageValid) {
			// Demande de l'image à traiter
			imagePath = getPathImageToProcess();
			try {
				sendImage(ImageIO.read(new File(imagePath)));
	            isImageValid = true;
	        } catch (IOException e) {
	            System.out.println("Image invalide");
	        }
		}
	}

	private static void sendImage(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", byteArrayOutputStream);
		output.writeInt(byteArrayOutputStream.size());
		output.write(byteArrayOutputStream.toByteArray());
		output.flush();
		byteArrayOutputStream.close();
	}
	
	private static void writeString(String message) throws IOException {
		output.writeUTF(message);
		output.flush();
	}
	
	private static void closeConnection() throws IOException {
		input.close();
		output.close();
		socket.close();
	}

}
