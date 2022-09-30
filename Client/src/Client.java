import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client {
    private static final Scanner sc = new Scanner(System.in);
    private static Socket socket;

    public static void main(String[] args) throws IOException {
        String serverAddress = getIpAddress();
        int serverPort = getPort();
        socket = new Socket(serverAddress, serverPort);
        System.out.format("The server is running %s:%d%n", serverAddress, serverPort);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        boolean success;
        do {
            String user = getUser();
            String pass = getPassword();
            out.writeUTF(user);
            out.flush();
            out.writeUTF(pass);
            out.flush();
            success = in.readBoolean();
            if (!success) {
                System.out.println("Erreur dans la saisie du mot de passe. Réessayer.");
            }
        } while (!success);
        System.out.println("Connection success.");
        boolean flag = true;
        while(flag) {
            System.out.println("Nom de l'image a modifier :");
            String imagePath = sc.nextLine();
            try {
                BufferedImage image = ImageIO.read(new File(imagePath));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", byteArrayOutputStream);
                byte[] size = ByteBuffer.allocate(4).putInt(byteArrayOutputStream.size()).array();
                out.write(size);
                out.write(byteArrayOutputStream.toByteArray());
                out.flush();
                flag = false;
            } catch (IOException e) {
                System.out.println("Image invalide");
            }
        }
        System.out.println("Nom de l'image modifier :");
        String newName = sc.nextLine();
        BufferedImage newImage = readImage(in);
        ImageIO.write(newImage, "jpg", new File(newName));
        socket.close();
    }

    private static String getPassword() {
        System.out.println("Mot de passe :");
        return sc.nextLine();
    }
    private static BufferedImage readImage(DataInputStream inputStream) throws IOException {
        byte[] sizeAr = new byte[4];
        int size = inputStream.read(sizeAr);
        byte[] imageAr = new byte[size];
        int test = inputStream.read(imageAr);
        System.out.println(test);
        return ImageIO.read(new ByteArrayInputStream(imageAr));
    }
    private static String getUser() {
        System.out.println("Utilisateur :");
        return sc.nextLine();
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
