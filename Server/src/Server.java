import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
        System.out.format("Le serveur fonctionne sur %s:%d%n\n", serverAddress, serverPort);
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
        private final int clientNumber;
        private String idUser;
        private final String serverAddress;
        private final int serverPort;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public ClientHandler(Socket socket, int clientNumber, String serverAddress, int serverPort) {
            this.socket = socket;
            this.clientNumber = clientNumber;
            this.serverAddress = serverAddress;
            this.serverPort = serverPort;

            System.out.println("Nouvelle connection du client#" + clientNumber + " ?? " + socket);
        }

        public void run() {
            try {
                dataIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dataOut = new DataOutputStream(socket.getOutputStream());
                idUser = dataIn.readUTF();
                boolean success = validateUser(idUser, dataIn.readUTF());
                dataOut.writeBoolean(success);
                if (success) {
                    getAndSendNewImage();
                    confirmationMessage();
                }
            } catch (IOException e) {
                System.out.println("Erreur de traitement du client#" + clientNumber + " : " + e);
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Impossible de fermer le socket");
                }
                System.out.println("Connection avec le client#" + clientNumber + " ferm??e");
            }
        }

        private void confirmationMessage() throws IOException {
            if (!dataIn.readBoolean()) {
                System.out.println("L'image n'a pas ??t?? re??ue correctement.");
            }else{
                System.out.println("L'image a ??t?? re??ue correctement.");
            }
        }

        private void getAndSendNewImage() throws IOException {
            BufferedImage image = readImage();
            printReceptionOfImageToTreat(idUser, dataIn.readUTF());
            try {
                sendImage(Sobel.process(image));
            } catch (IOException e) {
                System.out.println("Erreur de fichier");
            }
        }

        private void printReceptionOfImageToTreat(String client, String imageName) {
            // Inspir?? de : https://www.javatpoint.com/java-get-current-date#:~:text=Get%20Current%20Date%20and%20Time%3A%20java.time.format.,is%20included%20in%20JDK%201.8.
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String dateAndTime = dtf.format(now);

            // Impression
            String confirmationMessage = "[" + client + " - " + dateAndTime + "] : Image \"" + imageName + "\" re??ue pour traitement.\n";
            System.out.format(confirmationMessage, serverAddress, serverPort);
        }

        private void sendImage(BufferedImage image) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", byteArrayOutputStream);
            dataOut.writeInt(byteArrayOutputStream.size());
            dataOut.write(byteArrayOutputStream.toByteArray());
            dataOut.flush();
            byteArrayOutputStream.close();
        }

        private BufferedImage readImage() throws IOException {
            int size = dataIn.readInt();
            byte[] imageAr = new byte[size];
            dataIn.readFully(imageAr);
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
            System.out.println("Utilisateur cr???? : " + currentUser);
        }

    }
}