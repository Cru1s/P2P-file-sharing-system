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
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            String[] parts = request.split(" ");

            if (parts[0].equals("DOWNLOAD")) {
                String fileName = parts[1];
                if (availableFiles.contains(fileName)) {
                    try (FileInputStream fis = new FileInputStream(fileName);
                         BufferedReader fileReader = new BufferedReader(new InputStreamReader(fis))) {
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            out.println(line);
                        }
                    }
                } else {
                    out.println("File not found");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}