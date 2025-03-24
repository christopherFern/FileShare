package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientUI extends JFrame {
    private JList<String> localFileList;
    private JList<String> remoteFileList;
    private JButton downloadButton;
    private JButton uploadButton;
    private DefaultListModel<String> localListModel;
    private DefaultListModel<String> remoteListModel;
    private static final String SHARED_FOLDER = "src/main/java/Client/Files";
    private static final String SERVER_IP = "localhost"; // Change if needed
    private static final int SERVER_PORT = 5000;

    public ClientUI() {
        setTitle("File Sharing Client");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center window

        // Set background color
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout(10, 10));

        // Title label
        JLabel titleLabel = new JLabel("File Sharing Client", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Initialize list models
        localListModel = new DefaultListModel<>();
        remoteListModel = new DefaultListModel<>();

        // Initialize file lists
        localFileList = new JList<>(localListModel);
        remoteFileList = new JList<>(remoteListModel);
        localFileList.setBorder(BorderFactory.createTitledBorder("Local Files"));
        remoteFileList.setBorder(BorderFactory.createTitledBorder("Remote Files"));
        localFileList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        remoteFileList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Scroll panes
        JScrollPane localScroll = new JScrollPane(localFileList);
        JScrollPane remoteScroll = new JScrollPane(remoteFileList);
        localScroll.setPreferredSize(new Dimension(300, 250));
        remoteScroll.setPreferredSize(new Dimension(300, 250));

        // File list panel
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new GridLayout(1, 2, 10, 0));
        listPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        listPanel.setBackground(new Color(245, 245, 245));
        listPanel.add(localScroll);
        listPanel.add(remoteScroll);
        add(listPanel, BorderLayout.CENTER);

        // Initialize buttons
        downloadButton = new JButton("Download");
        uploadButton = new JButton("Upload");

        // Style buttons
        Color buttonColor = new Color(30, 144, 255);
        downloadButton.setBackground(buttonColor);
        uploadButton.setBackground(buttonColor);
        downloadButton.setForeground(Color.WHITE);
        uploadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);
        uploadButton.setFocusPainted(false);
        downloadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        uploadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        buttonPanel.add(downloadButton);
        buttonPanel.add(uploadButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadFile();
            }
        });

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadFile();
            }
        });

        // Create the shared folder if it doesn't exist
        File sharedFolder = new File(SHARED_FOLDER);
        if (!sharedFolder.exists()) {
            sharedFolder.mkdirs();
        }

        // Load files into lists
        refreshFileLists();
        refreshServerFileList();
    }

    private void refreshFileLists() {
        localListModel.clear();

        File sharedFolder = new File(SHARED_FOLDER);
        if (sharedFolder.exists() && sharedFolder.isDirectory()) {
            for (File file : sharedFolder.listFiles()) {
                if (file.isFile()) {
                    localListModel.addElement(file.getName());
                }
            }
        }
    }

    private void uploadFile() {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Sending the UPLOAD command to the server
            output.println("UPLOAD");

            // getting the name of the file that is selected
            String fileName = localFileList.getSelectedValue();
            if (fileName != null) {
                // sending the name of the selected file to the server
                output.println(fileName);

                // reads the content to the server
                File file = new File(SHARED_FOLDER + "/" + fileName);
                StringBuilder fileContent = new StringBuilder();
                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        fileContent.append(line).append("\n");
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Error reading the local file.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // sends the content to the server
                output.println(fileContent.toString());

                // Wait for a response from the server (optional)
                String response = input.readLine();
                if ("OK".equals(response)) {
                    JOptionPane.showMessageDialog(this, "File uploaded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Refresh file lists after upload
                    refreshFileLists();
                } else {
                    JOptionPane.showMessageDialog(this, "Error uploading file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a file to upload.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not connect to the server!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void downloadFile() {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
            output.println("DOWNLOAD");
            String fileName = remoteFileList.getSelectedValue();
            if (fileName != null){
                output.println(fileName);
                String line;
                StringBuilder fileContent = new StringBuilder();
                while ((line = input.readLine()) != null) {
                    if (line.equals("END")) {
                        break;
                    }
                    fileContent.append(line).append("\n");
                }
                File localFile = new File(SHARED_FOLDER + "/" + fileName);
                try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(localFile))) {
                    fileWriter.write(fileContent.toString());
                    // Refresh file lists after download
                    refreshFileLists();
                } catch (IOException e) {
                    System.out.println("Error writing the file: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            else {
                JOptionPane.showMessageDialog(this, "Please select a file to download.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error downloading file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void refreshServerFileList() {

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("DIR"); // Send DIR command
            String line;
            DefaultListModel<String> model = new DefaultListModel<>();

            while ((line = in.readLine()) != null) {
                if (line.equals("EMPTY")) { // Handle empty folder case
                    remoteFileList.setModel(new DefaultListModel<>()); // Clear the list
                    return;
                }
                if (line.equals("END")) break; // Stop reading
                model.addElement(line); // Add file to list
            }
            remoteFileList.setModel(model);

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not connect to server!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientUI().setVisible(true));
    }
}