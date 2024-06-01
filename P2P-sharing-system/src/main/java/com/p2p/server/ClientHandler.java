/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.p2p.server;

import com.p2p.common.PeerInfo;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Server server;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            String[] parts = request.split(" ");

            if (parts[0].equals("REGISTER")) {
                String peerId = parts[1];
                String ip = parts[2];
                int port = Integer.parseInt(parts[3]);
                server.registerPeer(peerId, ip, port);
                out.println("REGISTERED");
            } else if (parts[0].equals("DISCOVER")) {
                for (PeerInfo peer : server.getPeers()) {
                    out.println(peer.getIp() + " " + peer.getPort());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

