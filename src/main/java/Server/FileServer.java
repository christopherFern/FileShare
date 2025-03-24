package Server;

import java.io.File;
import Client.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    private static final String serverShareFolder = "src/main/java/Server/server_shared";
    private static final int PORT = 5000;

    public static void main(String[] args) {

        File sharedFolder = new File(serverShareFolder);
        if (!sharedFolder.exists()) {
            sharedFolder.mkdir();
        }

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for connections...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                // Create a new thread for the client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}