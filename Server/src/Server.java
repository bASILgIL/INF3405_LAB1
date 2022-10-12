import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;  

public class Server {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        int clientNumber = 1;
        String serverAddress = getIpAddress();
        int serverPort = getPort();
        ServerSocket listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("Le serveur fonctionne sur %s:%d%n\n", serverAddress,serverPort);
        try {
            while (true) {
                new ClientHandler(listener.accept(), clientNumber++, serverAddress, serverPort).start();
            }
        } finally {
            listener.close();
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

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private int clientNumber;
        private String serverAddress;
        private int serverPort;
        

        public ClientHandler(Socket socket, int clientNumber, String serverAddress, int serverPort) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;

            System.out.println("Nouvelle connection du client#" + clientNumber + " à " + socket);
        }

        public void run() {
            try {
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                String idUser = in.readUTF();
                String pass = in.readUTF();
                boolean success = validateUser(idUser, pass); //True if user and pass are good or new user
                out.writeBoolean(success);
                if (success) {
                    BufferedImage image = readImage(in); //https://stackoverflow.com/questions/25086868/how-to-send-images-through-sockets-in-java
                    printReceptionOfImageToTreat(idUser, in.readUTF());
                    try {
                        sendImage(Sobel.process(image), out);
                    } catch (IOException e) {
                        System.out.println("Erreur de fichier");
                    }
                    boolean received = in.readBoolean();
                    if (!received) {
                        System.out.println("L'image n'a pas été reçue correctement.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Erreur de traitement du client#" + clientNumber + " : " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Impossible de fermer le socket");
                }
                System.out.println("Connection avec le client#" + clientNumber + " fermée");
            }
        }
        
        private void printReceptionOfImageToTreat(String client, String imageName) {
        	// Inspiré de : https://www.javatpoint.com/java-get-current-date#:~:text=Get%20Current%20Date%20and%20Time%3A%20java.time.format.,is%20included%20in%20JDK%201.8.
        	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
        	LocalDateTime now = LocalDateTime.now(); 
        	String dateAndTime = dtf.format(now);
        	
        	// Impression
        	String confirmationMessage = "[" + client + " - " + "%s:%d%n" + " - " + dateAndTime + "] : Image " + imageName + " reçue pour traitement.\n";
        	System.out.format(confirmationMessage, serverAddress, serverPort);
        }

        private void sendImage(BufferedImage image, DataOutputStream out) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
            
            out.write(size);
            out.write(byteArrayOutputStream.toByteArray());
            out.flush();
        }

        private BufferedImage readImage(DataInputStream inputStream) throws IOException {
            byte[] sizeAr = new byte[4];
            inputStream.read(sizeAr);
            int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get();
            byte[] imageAr = new byte[size];
            inputStream.read(imageAr);
            return ImageIO.read(new ByteArrayInputStream(imageAr));
        }

        private boolean validateUser(String currentUser, String currentPass) throws IOException {
            try (BufferedReader br = new BufferedReader(new FileReader("Server/User.txt"))) {
				String line;
				while ((line = br.readLine()) != null) {
				    String[] lineSplit = line.split(";");
				    String savedUser = lineSplit[0];
				    String savedPass = lineSplit[1];
				    if (Objects.equals(savedUser, currentUser)) {
				        return Objects.equals(savedPass, currentPass);
				    }
				}
			} 
            createUser(currentUser, currentPass);
            return true;
        }

        private void createUser(String currentUser, String currentPass) throws IOException {
            BufferedWriter bw = new BufferedWriter(new FileWriter("Server/User.txt", true));
            bw.write(currentUser + ";" + currentPass);
            bw.newLine();
            bw.close();
            System.out.println("Utilisateur créé : " + currentUser);
        }

    }
}