package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.crypto_lib.KeyGenerator;
import pt.ulisboa.tecnico.sec.communication_lib.Communication;

import java.security.KeyPair;
import java.security.PublicKey;

import java.io.*;
import java.net.Socket;

public class Client {

    private final PublicKey _pubKey;
    private final Communication _communication;
    private Socket _clientSocket;

    public Client() {
        _pubKey = generateKeyPair(false);
        _communication = new Communication();
    }

    // TODO: move method to shared library "crypto_lib"
    public PublicKey generateKeyPair(boolean activateCC) {
        PublicKey pubKey = null;
        // OpenSSL
        if (activateCC == false) {
            KeyGenerator keyGen = new KeyGenerator();
            KeyPair keys = keyGen.generateKeyPair("RSA", 1024);
            // TODO: store private key in a keystore
            pubKey = keys.getPublic();
        }
        // CC
        else {
            // TODO
        }
        return pubKey;
    }

    public void startServerCommunication() {
        try {
            _clientSocket = new Socket("localhost", 8000);
            _communication.sendMessage("OLA\n", _clientSocket);
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
     * Posts an announcement to the Client's Board. This announcement
     * can refer to previous announcements.
     * @param message to be announced
     * @param reference to previous announcements
     */
    public void post(String message, String reference) {
        // TODO: client-server communication
    }

    /**
     * Posts an announcement to the General Board. This announcement
     * can refer to previous announcements.
     * @param message to be announced
     * @param reference to previous announcements
     */
    public void postGeneral(String message, String reference) {
        // TODO: client-server communication
    }

    /**
     * Retrieves the number latest announcements to be read
     * from the user's Board.
     * @param user whose Board is to be read
     * @param number of announcements to be retrieved
     */
    public void read(String user, int number) {
        // TODO: client-server communication
    }

    /**
     * Retrieves the number latest announcements to be read
     * from the General Board.
     * @param number of announcements to be retrieved
     */
    public void readGeneral(int number) {
        // TODO: client-server communication
    }

}
