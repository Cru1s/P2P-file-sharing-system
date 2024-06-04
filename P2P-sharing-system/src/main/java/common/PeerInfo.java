
package common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class PeerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String peerId;
    private String peerAddress;
    private int peerPort;

    public PeerInfo(String peerId, String peerAddress, int peerPort) {
        this.peerId = peerId;
        this.peerAddress = peerAddress;
        this.peerPort = peerPort;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(int peerPort) {
        this.peerPort = peerPort;
    }

    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(peerId);
        out.writeUTF(peerAddress);
        out.writeInt(peerPort);
    }

    public static PeerInfo read(DataInputStream in) throws IOException {
        String peerId = in.readUTF();
        String peerAddress = in.readUTF();
        int peerPort = in.readInt();
        return new PeerInfo(peerId, peerAddress, peerPort);
    }
}
