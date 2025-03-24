package Client;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    //constructor for the ClientHandler for the FileServer to call when receiving a connection
    public ClientHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        //creating an output and input variable related to the clientSocket
        try (BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);) {
            String command = input.readLine();
            //what the handler should do when receiving a DIR command
            if ("DIR".equals(command)) {
                //created sharedFolder using path starting from the project roo
                File sharedFolder = new File("src/main/java/Server/server_shared");
                File[] files = sharedFolder.listFiles(); // create an array of files
                if (files == null || files.length == 0) {
                    output.println("EMPTY"); // check if the array is empty
                }
                else {
                    for (File file : files) {
                        output.println(file.getName()); // send each filename back to the client
                    }
                }
                output.println("END"); // send END to the client so that the client know there are no more files
            }
            //What the handler should do when receiving UPLOAD command
            else if ("UPLOAD".equals(command)) {
                String fileName = input.readLine(); // the filename will be the second thing the client sends
                if (fileName == null) return; // if no file name is sent return

                File file = new File("src/main/java/Server/server_shared/" + fileName); // create new file in the server folder with the name received from the client
                //here using a bufferedWriter, the program goes writes each line being received from the client to the file that was just created
                try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file))) {
                    String line;
                    while ((line = input.readLine()) != null) {
                        if ("END".equals(line)) {
                            break;
                        }
                        fileWriter.write(line);
                        fileWriter.newLine();
                    }
                } catch (IOException e) {
                    System.out.println("Error writing the file: " + e.getMessage());//error handling
                    e.printStackTrace();
                }
            }
            //next this block will run when a client send DOWNLOAD to the server
            else if ("DOWNLOAD".equals(command)) {

                String fileName = input.readLine();// the next line after download will be the filename
                if (fileName == null) return;


                File file = new File("src/main/java/Server/server_shared/" + fileName);

                if (!file.exists()) {
                    output.println("ERROR: File not found");
                    return;
                }
                // here the server will read to the line by line to the client what is in the file that is being requested for download
                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        output.println(line);
                    }
                    output.println("END");// here the server will send END to the client to let it know there is no more in the file
                }
                //Error Handling
                catch (IOException e) {
                    System.out.println("Error reading the file: " + e.getMessage());
                    e.printStackTrace();
                }
            }


        }
        //more error handling in case something goes wrong with setting up the input reader or output writer
        catch (IOException e) {
            System.out.println("Error with the buffer reader or writer: " + e.getMessage());
            e.printStackTrace();

        } finally {
            try {
                clientSocket.close(); //close the connection
            }
            //error handling in case the connection does not close properly
            catch (IOException e) {
                System.out.println("Error closing the socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}