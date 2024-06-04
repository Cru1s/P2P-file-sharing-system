package server;

import common.PeerInfo;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Server server;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            String request = in.readUTF();
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
                        if (parts.length == 3) { // Ensure correct format
                            handleDiscover(parts[1], parts[2], out); // Pass the requesting peer ID
                        } else {
                            out.writeUTF("INVALID REQUEST");
                        }
                        break;
                    default:
                        out.writeUTF("INVALID REQUEST");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRegister(String[] parts, DataOutputStream out) throws IOException {
        if (parts.length < 4) {
            out.writeUTF("INVALID REQUEST");
            return;
        }
        String peerId = parts[1];
        String ip = parts[2];
        int port = Integer.parseInt(parts[3]);
        server.registerPeer(new PeerInfo(peerId, ip, port));
        out.writeUTF("REGISTERED");
    }

    private void handleShare(String[] parts, DataOutputStream out) throws IOException {
        if (parts.length < 3) {
            out.writeUTF("INVALID REQUEST");
            return;
        }
        String peerId = parts[1];
        String filePath = parts[2];
        server.addPeerFile(peerId, filePath);
        out.writeUTF("FILE SHARED");
    }

    private void handleDiscover(String requestingPeerId, String fileName, DataOutputStream out) throws IOException {
        System.out.println("Handling discover for peerId: " + requestingPeerId + ", fileName: " + fileName); // Debug statement
        for (PeerInfo peer : server.getPeers()) {
            List<String> files = server.getFilesForPeer(peer.getPeerId());
            System.out.println("Peer " + peer.getPeerId() + " has files: " + files); // Debug statement
            if (!peer.getPeerId().equals(requestingPeerId)) { // Exclude the requesting peer
                for (String filePath : files) {
                    if (filePath.endsWith(fileName)) { // Check if the file path ends with the requested file name
                        out.writeUTF(peer.getPeerId() + " " + peer.getPeerAddress() + " " + peer.getPeerPort());
                        out.flush(); // Ensure the response is sent immediately
                        System.out.println("Sent peer info for peer " + peer.getPeerId()); // Debug statement
                        break;
                    }
                }
            }
        }
        out.writeUTF("END");
        out.flush();
        System.out.println("End of discovery response"); // Debug statement
    }
}
