package peer;

import java.io.*;
import java.net.*;
import java.util.*;

public class PeerHandler extends Thread {
    private Socket socket;
    private List<String> availableFiles;

    public PeerHandler(Socket socket, List<String> availableFiles) {
        this.socket = socket;
        this.availableFiles = availableFiles;
    }

    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            String request = in.readUTF();
            if (request != null) {
                handleRequest(request, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(String request, DataOutputStream out) throws IOException {
        String[] tokens = request.split(" ");
        String command = tokens[0];

        if ("DOWNLOAD".equals(command)) {
            String requestingPeerIp = socket.getInetAddress().getHostAddress();
            String fileName = tokens[1];
            sendFileToPeer(requestingPeerIp, fileName, out);
        }
    }

    private void sendFileToPeer(String requestingPeerIp, String fileName, DataOutputStream out) throws IOException {
        boolean fileFound = false;
        for (String filePath : availableFiles) {
            if (filePath.endsWith(fileName)) {  // This checks for the file name at the end of the path
                File file = new File(filePath);
                if (file.exists()) {
                    fileFound = true;
                    out.writeUTF("FOUND");
                    out.flush();
                    System.out.println("Sending file: " + filePath);
                    sendFile(file, out);
                    break;
                }
            }
        }
        if (!fileFound) {
            out.writeUTF("NOT_FOUND");
            out.flush();
            System.out.println("File not found: " + fileName);
        }
    }

    private void sendFile(File file, DataOutputStream out) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            System.out.println("File sent successfully.");
        } catch (IOException e) {
            System.err.println("Error sending file: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
