
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

public class Server {
    private static final Scanner sc = new Scanner(System.in);
    private static ServerSocket listener;

    public static void main(String[] args) throws IOException {
        int clientNumber = 0;
        String serverAddress = getIpAddress();
        int serverPort = getPort();
        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);

        listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("The server is running %s:%d%n", serverAddress, serverPort);

        try {
            while (true) {
                new ClientHandler(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
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

    private static class ClientHandler extends Thread {
        private Socket socket;
        private int clientNumber;

        public ClientHandler(Socket socket, int clientNumber) {
            this.socket = socket;
            this.clientNumber = clientNumber;

            System.out.println("New connection with client#" + clientNumber + " at " + socket);
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
                    sendImage(Sobel.process(image), out);
                }
            } catch (IOException e) {
                System.out.println("error handling client#" + clientNumber + " : " + e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Could not close a socket");
                }
                System.out.println("Connection with client#" + clientNumber + " closed");
            }
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
            int size = inputStream.read(sizeAr);
            byte[] imageAr = new byte[size];
            int test = inputStream.read(imageAr);
            System.out.println(test);
            return ImageIO.read(new ByteArrayInputStream(imageAr));
        }

        private boolean validateUser(String currentUser, String currentPass) throws IOException {
            BufferedReader br = new BufferedReader(new FileReader("user.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] lineSplit = line.split(";");
                String savedUser = lineSplit[0];
                String savedPass = lineSplit[1];
                if (Objects.equals(savedUser, currentUser)) {
                    return Objects.equals(savedPass, currentPass);
                }
            }
            createUser(currentUser, currentPass);
            return true;
        }

        private void createUser(String currentUser, String currentPass) throws IOException {
            BufferedWriter bw = new BufferedWriter(new FileWriter("user.txt", true));
            bw.write(currentUser + ";" + currentPass);
            bw.close();
        }

    }
}
