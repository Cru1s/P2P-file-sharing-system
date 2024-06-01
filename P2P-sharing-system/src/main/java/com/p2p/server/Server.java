/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.p2p.server;

import com.p2p.common.PeerInfo;
import java.io.*;
import java.net.*;
import java.util.*;


public class Server {
    private Map<String, PeerInfo> peers;

    public Server() {
        peers = new HashMap<>();
    }

    public synchronized void registerPeer(String peerId, String ip, int port) {
        peers.put(peerId, new PeerInfo(ip, port));
    }

    public synchronized List<PeerInfo> getPeers() {
        return new ArrayList<>(peers.values());
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
