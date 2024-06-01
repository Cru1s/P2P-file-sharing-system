/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.p2p.server;

import com.p2p.common.PeerInfo;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private final Map<String, PeerInfo> peers;
    private final Map<String, List<String>> peerFiles;  // Tracks files shared by each peer

    public Server() {
        peers = new HashMap<>();
        peerFiles = new HashMap<>();
    }

    public synchronized void registerPeer(String peerId, String ip, int port) {
        peers.put(peerId, new PeerInfo(ip, port));
        peerFiles.putIfAbsent(peerId, new ArrayList<>());  // Initialize file list for new peers
        System.out.println("Registered peer: " + peerId);
        printCurrentState();
    }

    public synchronized void addPeerFile(String peerId, String filePath) {
        List<String> files = peerFiles.computeIfAbsent(peerId, k -> new ArrayList<>());
        files.add(filePath);
        peerFiles.put(peerId, files);  // Update the file list for the peer
        System.out.println("Added file for peer: " + peerId + ", file: " + filePath);
        printCurrentState();
    }

    public synchronized List<PeerInfo> getPeers() {
        return new ArrayList<>(peers.values());
    }

    public synchronized List<String> getFilesForPeer(String peerId) {
        return peerFiles.getOrDefault(peerId, new ArrayList<>());
    }
    
    private void printCurrentState() {
        System.out.println("Current State:");
        for (Map.Entry<String, PeerInfo> entry : peers.entrySet()) {
            System.out.println("Peer: " + entry.getKey() + ", IP: " + entry.getValue().getIp() + ", Port: " + entry.getValue().getPort());
            List<String> files = peerFiles.get(entry.getKey());
            if (files != null) {
                for (String file : files) {
                    System.out.println("  File: " + file);
                }
            }
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server started on port 8080");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, this)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}