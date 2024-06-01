/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.p2p.peer;

import java.io.*;
import java.net.*;
import java.util.*;

public class PeerHandler implements Runnable {
    private Socket clientSocket;
    private List<String> availableFiles;

    public PeerHandler(Socket clientSocket, List<String> availableFiles) {
        this.clientSocket = clientSocket;
        this.availableFiles = availableFiles;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream();
             PrintWriter writer = new PrintWriter(out, true)) {

            String request = in.readLine();
            if (request != null) {
                System.out.println("Request received: " + request);
                String[] parts = request.split(" ");

                if (parts.length > 1 && parts[0].equals("DOWNLOAD")) {
                    handleFileDownload(parts[1], out, writer);
                } else {
                    writer.println("Invalid request");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling peer request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleFileDownload(String fileName, OutputStream out, PrintWriter writer) {
        boolean fileFound = false;
        try {
            for (String filePath : availableFiles) {
                if (filePath.endsWith(fileName)) {  // This checks for the file name at the end of the path
                    fileFound = true;
                    File file = new File(filePath);
                    writer.println("OK");
                    writer.flush();
                    System.out.println("Sending file: " + filePath);

                    sendFile(file, out);
                    break;
                }
            }
            if (!fileFound) {
                writer.println("File not found");
                System.out.println("File not found: " + fileName);
            }
        } catch (Exception e) {
            writer.println("Error sending file");
            System.err.println("Error sending file: " + fileName + ", " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendFile(File file, OutputStream out) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            System.out.println("File sent successfully.");
        }
    }
}