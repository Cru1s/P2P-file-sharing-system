package peer;

import common.PeerInfo;
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
        System.out.println("Attempting to register with server...");
        try (Socket socket = new Socket(serverIp, serverPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF("REGISTER " + peerId + " " + InetAddress.getLocalHost().getHostAddress() + " " + peerPort);
            String response = in.readUTF();
            System.out.println("Server response: " + response);
            if ("REGISTERED".equals(response)) {
                System.out.println("Peer registered successfully");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<PeerInfo> discoverPeers(String fileName) {
        System.out.println("Discovering peers...");
        List<PeerInfo> peers = new ArrayList<>();
        try (Socket socket = new Socket(serverIp, serverPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF("DISCOVER " + peerId + " " + fileName); // Pass the peer ID and file name to the server
            String response;
            while (true) {
                response = in.readUTF();
                if ("END".equals(response)) {
                    break;
                }
                System.out.println("Discovered peer: " + response);
                String[] parts = response.split(" ");
                if (parts.length == 3) {
                    peers.add(new PeerInfo(parts[0], parts[1], Integer.parseInt(parts[2])));
                } else {
                    System.err.println("Invalid peer information received: " + response);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Peers discovered: " + peers.size());
        return peers;
    }

    public boolean downloadFile(String fileName, PeerInfo peerInfo) {
        System.out.println("Downloading file: " + fileName + " from peer: " + peerInfo.getPeerAddress() + " port: " + peerInfo.getPeerPort());
        try (Socket socket = new Socket(peerInfo.getPeerAddress(), peerInfo.getPeerPort());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream());
             FileOutputStream fos = new FileOutputStream(fileName)) {

            out.writeUTF("DOWNLOAD " + fileName);
            out.flush();

            String response = in.readUTF();
            System.out.println("Peer response: " + response);
            if ("NOT_FOUND".equals(response)) {
                System.out.println("File not found on peer: " + peerInfo.getPeerAddress() + " port: " + peerInfo.getPeerPort());
                return false;
            }

            if ("FOUND".equals(response)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while (true) {
                    try {
                        bytesRead = in.read(buffer);
                        if (bytesRead == -1) {
                            break;
                        }
                        fos.write(buffer, 0, bytesRead);
                    } catch (EOFException e) {
                        break; // Exit loop on end of file
                    }
                }
                System.out.println("File " + fileName + " downloaded successfully.");
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void shareFile(String filePath) {
        System.out.println("Sharing file: " + filePath);
        availableFiles.add(filePath);
        try (Socket socket = new Socket(serverIp, serverPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF("SHARE " + peerId + " " + filePath);
            String response = in.readUTF();
            System.out.println("Server response: " + response);
            if ("FILE SHARED".equals(response)) {
                System.out.println("File shared successfully");
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
            System.out.println("3. Kill yourself");
            String choice = scanner.next();
            scanner.nextLine();  // consume newline
            switch (choice) {
                case "1":
                    System.out.println("Enter the file path:");
                    String filePath = scanner.nextLine();
                    shareFile(filePath);
                    break;
                case "2":
                    System.out.println("Enter the file name:");
                    String fileName = scanner.nextLine();
                    List<PeerInfo> peers = discoverPeers(fileName);
                    if (peers.isEmpty()) {
                        System.out.println("No peers have the file: " + fileName);
                        continue;
                    }   System.out.println("Select a peer to download from:");
                    for (int i = 0; i < peers.size(); i++) {
                        PeerInfo peer = peers.get(i);
                        System.out.println(i + 1 + ". " + peer.getPeerAddress() + " port: " + peer.getPeerPort());
                    }   int peerChoice = scanner.nextInt();
                    scanner.nextLine();  // consume newline
                    if (peerChoice > 0 && peerChoice <= peers.size()) {
                        PeerInfo selectedPeer = peers.get(peerChoice - 1);
                        downloadFile(fileName, selectedPeer);
                    } else {
                        System.out.println("Invalid choice. Please try again.");
                    }   
                    break;
                case "3":
                    System.exit(0);
                default:
                    System.out.println("Invalid choice, try again");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        Peer peer = new Peer("peer7", "localhost", 8080, 2200);
        peer.start();
    }
}
