package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import java.io.*;
import java.net.Socket;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class Client {

    private PublicKey _pubKey; // TODO: final?
    private final Communication _communication;
    private Socket _clientSocket;
    private UUIDGenerator _uuidGenerator;

    // TODO: make register method, maybe receive input to know which client key to retrieve
    public Client() {
        try {
            _pubKey = KeyPairUtil.loadPublicKey("src/main/resources/crypto/public.key");
        } catch (IOException e) {

        } catch (NoSuchAlgorithmException e) {

        } catch (InvalidKeySpecException e) {

        }
        _communication = new Communication();
        _uuidGenerator = new UUIDGenerator();
    }

    // TODO: move method to shared library "crypto_lib"
    /*public PublicKey generateKeyPair(boolean activateCC) {
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
    }*/

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

    // TODO: make register method, see if _pubKey should be assigned here
    public void register() {

        // TODO: client-server communication
    }

    /**
     * Posts an announcement to the Client's Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     */
    public void post(String message, List<Integer> references) {
        int uuid = _uuidGenerator.generateUUID();
        System.out.println("Posting announcement to user's Board with uuid: " + uuid);
        // TODO: client-server communication
    }

    /**
     * Posts an announcement to the General Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     */
    public void postGeneral(String message, List<Integer> references) {
        int uuid = _uuidGenerator.generateUUID();
        System.out.println("Posting announcement to General Board:");
        System.out.println("* UUID: " + uuid);
        System.out.println("* Message: " + message);
        System.out.println("* References: ");
        for (int r : references)
            System.out.println("  * " + r);
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
