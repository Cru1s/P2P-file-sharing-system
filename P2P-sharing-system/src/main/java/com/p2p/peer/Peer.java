/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.p2p.peer;

import com.p2p.common.PeerInfo;
import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
    private String peerId;
    private String serverIp;
    private int serverPort;
    private List<String> availableFiles;
    private int peerPort;

    public Peer(String peerId, String serverIp, int serverPort, int peerPort) {
        this.peerId = peerId;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.peerPort = peerPort;
        this.availableFiles = new ArrayList<>();
    }

    public void registerWithServer() {
        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("REGISTER " + peerId + " " + InetAddress.getLocalHost().getHostAddress() + " " + peerPort);
            String response = in.readLine();
            if ("REGISTERED".equals(response)) {
                System.out.println("Peer registered successfully");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<PeerInfo> discoverPeers() {
        List<PeerInfo> peers = new ArrayList<>();
        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("DISCOVER");
            String response;
            while ((response = in.readLine()) != null) {
                String[] parts = response.split(" ");
                peers.add(new PeerInfo(parts[0], Integer.parseInt(parts[1])));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return peers;
    }

    public void shareFile(String filePath) {
        availableFiles.add(filePath);
    }

    public void downloadFile(String fileName, PeerInfo peerInfo) {
        try (Socket socket = new Socket(peerInfo.getIp(), peerInfo.getPort());
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             FileOutputStream fos = new FileOutputStream(fileName)) {

            out.println("DOWNLOAD " + fileName);
            String response;
            while ((response = in.readLine()) != null) {
                fos.write(response.getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(peerPort)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new PeerHandler(clientSocket, availableFiles)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        registerWithServer();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Share a file");
            System.out.println("2. Download a file");
            int choice = scanner.nextInt();
            scanner.nextLine();  // consume newline

            if (choice == 1) {
                System.out.println("Enter the file path:");
                String filePath = scanner.nextLine();
                shareFile(filePath);
            } else if (choice == 2) {
                System.out.println("Enter the file name:");
                String fileName = scanner.nextLine();
                List<PeerInfo> peers = discoverPeers();
                for (PeerInfo peer : peers) {
                    downloadFile(fileName, peer);
                }
            }
        }
    }

    public static void main(String[] args) {
        Peer peer = new Peer("peer1", "localhost", 8080, 8081);
        peer.start();
    }
}
