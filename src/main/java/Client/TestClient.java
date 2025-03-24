package Client;
import java.io.*;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try (
                Socket socket = new Socket("localhost", 5000);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            // Send the DOWNLOAD command
            out.println("DOWNLOAD");

            // Send the file name
            String fileName = "testfile.txt";
            out.println(fileName);

            // Receive and print the file content from the server
            String line;
            while ((line = in.readLine()) != null) {
                if ("END".equals(line)) {
                    break; // Stop reading when "END" is received
                }
                System.out.println("Received line: " + line); // Print each line received
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}