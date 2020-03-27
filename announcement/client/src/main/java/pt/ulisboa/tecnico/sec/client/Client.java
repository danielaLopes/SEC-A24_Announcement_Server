package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationValueVisitor;

public class Client{

    private PublicKey _pubKey;
    private PrivateKey _privateKey;

    private List<PublicKey> _otherUsersPubKeys;
    private PublicKey _serverPubKey;

    private final Communication _communication;
    private ObjectOutputStream _oos;
    private ObjectInputStream _ois;
    private Socket _clientSocket;

    public Client(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias,
                  String serverPubKeyPath, List<String> otherUsersPubKeyPaths) {
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);
        
        loadServerPublicKey(serverPubKeyPath);

        _otherUsersPubKeys = new ArrayList<PublicKey>();
        loadOtherUsersPubKeys(otherUsersPubKeyPaths);

        _communication = new Communication();

    }

    /**
     * Loads this Client's public key to _privateKey.
     */
    public void loadPublicKey(String pubKeyPath) {
        try {
            _pubKey = KeyPairUtil.loadPublicKey(pubKeyPath);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load public key.\n" + e);
            System.exit(-1);
        }
    }

    /**
     * Loads this Client's private key to _privateKey.
     */
    public void loadPrivateKey(String keyStorePath, String keyStorePasswd, String entryPasswd, String alias) {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStorage.loadKeyStore(keyStorePasswd.toCharArray(), keyStorePath);
        } catch(Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load keystore.\n" + e);
            System.exit(-1);
        }
        try {
            _privateKey = KeyStorage.loadPrivateKey(entryPasswd.toCharArray(), alias, keyStore);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load private key.\n" + e);
            System.exit(-1);
        }
    }

    /**
     * Loads server's public key to _serverPubKey.
     */
    public void loadServerPublicKey(String path) {
        try {
            System.out.println(path);
            _serverPubKey = KeyPairUtil.loadPublicKey(path);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load server's public key.\n" + e);
            System.exit(-1);
        }
    }

    /**
     * Loads other user's public keys to _otherUsersPubKeys.
     */
    public void loadOtherUsersPubKeys(List<String> paths) {
        for (String path : paths) {
            try {
                _otherUsersPubKeys.add(KeyPairUtil.loadPublicKey(path));
            } catch (Exception e) {
                System.out.println("Error: Not possible to initialize client because it was not possible to load other users public keys.\n" + e);
                System.exit(-1);
            }
        }
    }

    /**
     * Prints other user's public keys.
     */
    public void printOtherUsersPubKeys() {
        for (int i = 0; i < _otherUsersPubKeys.size(); i++) {
            System.out.println("* " + i + ": " + _otherUsersPubKeys.get(i));
        }
    }

    /**
     * Retrieves public key from a specific user.
     * @param userIndex
     */
    public PublicKey getPublicKeyFromUser(int userIndex) {
        return _otherUsersPubKeys.get(userIndex);
    }

    public void startServerCommunication() {
        try {
            _clientSocket = new Socket("localhost", 8888);

            _oos = new ObjectOutputStream(_clientSocket.getOutputStream());
            _ois = new ObjectInputStream(_clientSocket.getInputStream());

            register();
        }
        catch(IOException e) {
            System.out.println("Error starting client socket");
        }
    }

    public void closeCommunication() {
        try {
            ProtocolMessage pm = new ProtocolMessage("LOGOUT");
            VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
            _communication.sendMessage(vpm, _oos);
            _communication.close(_clientSocket);
        }
        catch(IOException e) {
            System.out.println("Error closing socket");
        }   
    }

    public void printStatusCodeDescription(StatusCode sc) {
        System.out.println("======" + sc.getDescription() + "======");
    }

    public VerifiableProtocolMessage createVerifiableMessage(ProtocolMessage pm) {
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(pm);
            byte[] signedpm = SignatureUtil.sign(bpm, _privateKey);
            return new VerifiableProtocolMessage(pm, signedpm);
        }
        catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e) { 
            System.out.println(e);
        }
        return null;
    }

    public boolean verifySignature(VerifiableProtocolMessage vpm) {
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(vpm.getProtocolMessage());
            return SignatureUtil.verifySignature(vpm.getSignedProtocolMessage(), _serverPubKey, bpm);
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Error: Algorithm used to verify signature is not valid.\n" + e);
            // TODO: return statusCode?
        }
        catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
        } 
        catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
        }
        return false;
    }

    // TODO: make register method, see if _pubKey should be assigned here
    public void register() {
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage("REGISTER", _pubKey, uuid);
        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        try {
            _communication.sendMessage(vpm, _oos);
            VerifiableProtocolMessage rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);

            if (verifySignature(rvpm)) {
                System.out.println("Server signature verified successfully");
                printStatusCodeDescription(rvpm.getProtocolMessage().getStatusCode());
            }
            else {
                System.out.println("Could not verify server signature");
                closeCommunication();
                System.exit(-1);  
            }
        }
        catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }
    }

    /**
     * Posts an announcement to the Client's Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     */
    public boolean post(String message, List<Integer> references) {
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return false;
        }
        Announcement a = new Announcement(message, references);
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage("POST", _pubKey, uuid, a);
        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        try {
            _communication.sendMessage(vpm, _oos);
            VerifiableProtocolMessage rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);

            if (verifySignature(rvpm)) {
                System.out.println("Server signature verified successfully");
                printStatusCodeDescription(rvpm.getProtocolMessage().getStatusCode());
            }
            else {
                System.out.println("Could not verify server signature");
                closeCommunication();
                System.exit(-1);  
            }
        }
        catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        return true;
    }

    /**
     * Posts an announcement to the General Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     */
    public boolean postGeneral(String message, List<Integer> references) {
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return false;
        }
        Announcement a = new Announcement(message, references);
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage("POSTGENERAL", _pubKey, uuid, a);
        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        try {
            _communication.sendMessage(vpm, _oos);
            VerifiableProtocolMessage rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);

            if (verifySignature(rvpm)) {
                System.out.println("Server signature verified successfully");
                printStatusCodeDescription(rvpm.getProtocolMessage().getStatusCode());
            }
            else {
                System.out.println("Could not verify server signature");
                closeCommunication();
                System.exit(-1);  
            }
        }
        catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        return true;
    }

    /**
     * Retrieves the number latest announcements to be read
     * from the user's Board.
     * @param user whose Board is to be read
     * @param number of announcements to be retrieved
     */
    public boolean read(int user, int number) {
        if (invalidNumberOfAnnouncements(number)) {
            System.out.println("Minimum number of announcements to read is 1.");
            return false;
        }
        if (invalidUser(user)) {
            System.out.println("User does not exist.");
            return false;
        }

        // TODO: client-server communication

        return true;
    }

    /**
     * Retrieves the number latest announcements to be read
     * from the General Board.
     * @param number of announcements to be retrieved
     */
    public boolean readGeneral(int number) {
        if (invalidNumberOfAnnouncements(number)) {
            System.out.println("Minimum number of announcements to read is 1.");
            return false;
        }

        // TODO: client-server communication

        return true;
    }

    /**
     * Verifies if a message has valid length.
     * @param message
     */
    public boolean invalidMessageLength(String message) {
        return message.length() >= 255;
    }

    /**
     * Verifies if a number of announcements to be retrieved is valid.
     * @param number
     */
    public boolean invalidNumberOfAnnouncements(int number) {
        return number <= 0;
    }

    /**
     * Verifies if a user exists.
     * @param user
     */
    public boolean invalidUser(int user) {
        return user < 0 || user >= _otherUsersPubKeys.size();
    }

}
