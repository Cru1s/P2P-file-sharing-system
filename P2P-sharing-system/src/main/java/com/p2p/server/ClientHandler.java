/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.p2p.server;

import com.p2p.common.PeerInfo;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String request = in.readLine();
            if (request != null) {
                String[] parts = request.split(" ");
                switch (parts[0]) {
                    case "REGISTER":
                        handleRegister(parts, out);
                        break;
                    case "SHARE":
                        handleShare(parts, out);
                        break;
                    case "DISCOVER":
                        handleDiscover(out);
                        break;
                    default:
                        out.println("INVALID REQUEST");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRegister(String[] parts, PrintWriter out) {
        if (parts.length < 4) {
            out.println("INVALID REQUEST");
            return;
        }
        String peerId = parts[1];
        String ip = parts[2];
        int port = Integer.parseInt(parts[3]);
        server.registerPeer(peerId, ip, port);
        out.println("REGISTERED");
    }

    private void handleShare(String[] parts, PrintWriter out) {
        if (parts.length < 3) {
            out.println("INVALID REQUEST");
            return;
        }
        String peerId = parts[1];
        String filePath = parts[2];
        server.addPeerFile(peerId, filePath);
        out.println("FILE SHARED");
    }

    private void handleDiscover(PrintWriter out) {
        for (PeerInfo peer : server.getPeers()) {
            out.println(peer.getIp() + " " + peer.getPort());
        }
    }
}
