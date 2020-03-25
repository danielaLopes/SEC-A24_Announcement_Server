package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;

import java.net.Socket;
import java.util.UUID;

import java.security.*;

import java.io.*;
import java.util.List;

public class Client{

    private PublicKey _pubKey; // TODO: final?
    private PrivateKey _privKey; // TODO: final?

    private final Communication _communication;
    private Socket _clientSocket;

    public Client() {
        _communication = new Communication();

        this.setKeyPair();
    }

    public void setKeyPair() {
        KeyPair keys = KeyPairUtil.generateKeyPair("RSA", 1024);
        _pubKey = keys.getPublic();
        _privKey = keys.getPrivate();
    }

    public void startServerCommunication() {
        try {
            _clientSocket = new Socket("localhost", 8000);
            register();
        }
        catch(IOException e) {
            System.out.println("Error starting client socket");
        }
    }

    public void closeCommunication() {
        try {
            _communication.close(_clientSocket);
        }
        catch(IOException e) {
            System.out.println("Error closing socket");
        }   
    }


    /**
     * Padds a string with zeros on the left, so that it 
     * becomes with fixed size.
     * @param s string to be padded
     * @param size final string size
     */
    public String paddingRightZeros(String s, int size) {
        return String.format("%1$-" + size + "s", s).replace(' ', '0');
    }

    // TODO: make register method, see if _pubKey should be assigned here
    public void register() {
        String message = paddingRightZeros("REGISTER", 30);
        ProtocolMessage pm = new ProtocolMessage(message);
        try {
            _communication.sendMessage(pm, _clientSocket);
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Posts an announcement to the Client's Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     */
    public void post(String message, List<Integer> references) {
        // TODO: verify parameters
        // TODO: client-server communication
    }

    /**
     * Posts an announcement to the General Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     */
    public void postGeneral(String message, List<Integer> references) {
        // TODO: verify parameters
        // TODO: client-server communication
    }

    /**
     * Retrieves the number latest announcements to be read
     * from the user's Board.
     * @param user whose Board is to be read
     * @param number of announcements to be retrieved
     */
    public void read(String user, int number) {
        // TODO: verify parameters
        // TODO: client-server communication
    }

    /**
     * Retrieves the number latest announcements to be read
     * from the General Board.
     * @param number of announcements to be retrieved
     */
    public void readGeneral(int number) {
        // TODO: verify parameters
        // TODO: client-server communication
    }

}
