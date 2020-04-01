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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class Client{

    private PublicKey _pubKey;
    private PrivateKey _privateKey;

    private List<PublicKey> _usersPubKeys;
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

        _usersPubKeys = new ArrayList<PublicKey>();
        loadUsersPubKeys(otherUsersPubKeyPaths);

        _communication = new Communication();
        startServerCommunication();
    }

    /**
     * Loads Client's public key to _pubKey.
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
     * Loads Client's private key to _privateKey.
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
        if (_privateKey == null) {
            System.out.println("Error: Not possible to initialize server because it " +
                    "was not possible to load private key due to wrong password or alias.\n");
            System.exit(-1);
        }
    }

    /**
     * Loads server's public key to _serverPubKey.
     */
    public void loadServerPublicKey(String path) {
        try {
            _serverPubKey = KeyPairUtil.loadPublicKey(path);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load server's public key.\n" + e);
            System.exit(-1);
        }
    }

    /**
     * Loads other known user's public keys to _usersPubKeys.
     */
    public void loadUsersPubKeys(List<String> paths) {
        _usersPubKeys.add(_pubKey);
        for (String path : paths) {
            try {
                _usersPubKeys.add(KeyPairUtil.loadPublicKey(path));
            } catch (Exception e) {
                System.out.println("Error: Not possible to initialize client because it was not possible to load other users public keys.\n" + e);
                System.exit(-1);
            }
        }
    }

    /**
     * Prints other known user's public keys.
     */
    public void printOtherUsersPubKeys() {
        for (int i = 0; i < _usersPubKeys.size(); i++) {
            System.out.println("* " + i + ": " + _usersPubKeys.get(i));
        }
    }

    /**
     * Retrieves the public key of a specific user.
     * @param userIndex describes the index of a user in _usersPubKeys
     */
    public PublicKey getPublicKeyFromUser(int userIndex) {
        return _usersPubKeys.get(userIndex);
    }

    /**
     * Starts the communication with the server for future operations.
     */
    public void startServerCommunication() {
        try {
            _clientSocket = new Socket("localhost", 8888);

            _oos = new ObjectOutputStream(_clientSocket.getOutputStream());
            _ois = new ObjectInputStream(_clientSocket.getInputStream());

            register();
        }
        catch(IOException e) {
            System.out.println("Error starting client socket. Make sure the server is running.");
        }
    }

    /**
     * Closes the communication with the server.
     */
    public void closeCommunication() {
        try {
            ProtocolMessage pm = new ProtocolMessage("LOGOUT");
            VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
            _communication.sendMessage(vpm, _oos);
            _communication.close(_clientSocket);
        }
        catch(IOException e) {
            System.out.println("Error closing socket.");
        }   
    }

    /**
     * Prints a status code. Can be mostly used to analyze the responses
     * given by the server.
     */
    public void printStatusCode(StatusCode sc) {
        System.out.println("Status Code: ====== " + sc.getCode() + ": " + sc.getDescription() + " ======");
    }

    /**
     * Creates a VerifiableProtocolMessage with pm and signed pm, in order
     * to avoid messages being tampered.
     * @param pm is a ProtocolMessage
     * @return the VerificableProtocolMessage to be sent
     */
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

    /**
     * Verifies if the signed ProtocolMessage inside vpm is valid, i.e.
     * if we sign the ProtocolMessage inside vpm it should be equivalent
     * to its signed instance.
     * @param vpm is a VerifiableProtocolMessage
     * @return a boolean, expressing if the signature is valid or not
     */
    public boolean verifySignature(VerifiableProtocolMessage vpm) {
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(vpm.getProtocolMessage());
            return SignatureUtil.verifySignature(vpm.getSignedProtocolMessage(), _serverPubKey, bpm);
        }
        catch (NoSuchAlgorithmException e) {
            System.out.println("Error: Algorithm used to verify signature is not valid.\n" + e);
        }
        catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
        } 
        catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
        }
        return false;
    }

    /**
     * Retrieves the StatusCode of a VerifiableProtocolMessage.
     * @param vpm is a VerifiableProtocolMessage
     * @return a StatusCode
     */
    public StatusCode getStatusCodeFromVPM(VerifiableProtocolMessage vpm) {
        return vpm.getProtocolMessage().getStatusCode();
    }

    /**
     * Retrieves the list of announcements of a VerifiableProtocolMessage.
     * @param vpm is a VerifiableProtocolMessage
     * @return list of announcements
     */
    public List<Announcement> getAnnouncementsFromVPM(VerifiableProtocolMessage vpm) {
        return vpm.getProtocolMessage().getAnnouncements();
    }

    public VerifiableProtocolMessage requestServer(ProtocolMessage pm) {
        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        VerifiableProtocolMessage rvpm = null;
        StatusCode rsc = null;

        try {
            _communication.sendMessage(vpm, _oos);
            rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);
            rsc = getStatusCodeFromVPM(rvpm);

            if (verifySignature(rvpm)) {
                System.out.println("Server signature verified successfully");
                printStatusCode(rsc);
            }
            else {
                System.out.println("Could not register: could not verify server signature");
                closeCommunication();
                System.exit(-1);
            }
        }
        catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        return rvpm;
    }

    /**
     * Allows the Client to register in the Server, hence providing its Public Key.
     * Must be the first operation to be done in the Client-Server communication.
     * @return the StatusCode of the operation
     */
    public int register() {
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage("REGISTER", _pubKey, uuid);
        VerifiableProtocolMessage vpm = requestServer(pm);
        StatusCode rsc = getStatusCodeFromVPM(vpm);

        return rsc.getCode();
    }

    /**
     * Posts an announcement to the Client's Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    public int post(String message, List<Integer> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return StatusCode.NULL_FIELD.getCode();
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return StatusCode.NULL_FIELD.getCode();
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return StatusCode.INVALID_MESSAGE_LENGTH.getCode();
        }

        Announcement a = new Announcement(message, references);
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage("POST", _pubKey, uuid, a);
        VerifiableProtocolMessage vpm = requestServer(pm);
        StatusCode rsc = getStatusCodeFromVPM(vpm);

        return rsc.getCode();
    }

    /**
     * Posts an announcement to the General Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    public int postGeneral(String message, List<Integer> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return StatusCode.NULL_FIELD.getCode();
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return StatusCode.NULL_FIELD.getCode();
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return StatusCode.INVALID_MESSAGE_LENGTH.getCode();
        }

        Announcement a = new Announcement(message, references);
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage("POSTGENERAL", _pubKey, uuid, a);
        VerifiableProtocolMessage vpm = requestServer(pm);
        StatusCode rsc = getStatusCodeFromVPM(vpm);

        return rsc.getCode();
    }

    /**
     * Retrieves the number latest announcements from the user's Board.
     * @param user 
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    public AbstractMap.SimpleEntry<Integer, List<Announcement>> read(int user, int number) {
        if (invalidUser(user)) {
            System.out.println("Invalid user.");
            return new AbstractMap.SimpleEntry<>(StatusCode.USER_NOT_REGISTERED.getCode(), new ArrayList<>());
        }
        PublicKey userToReadPB = _usersPubKeys.get(user);
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, uuid, number, userToReadPB);
        VerifiableProtocolMessage vpm = requestServer(pm);

        StatusCode rsc = getStatusCodeFromVPM(vpm);
        List<Announcement> announcements = getAnnouncementsFromVPM(vpm);

        return new AbstractMap.SimpleEntry<>(rsc.getCode(), announcements);
    }

    /**
     * Retrieves the number latest announcements from the General Board.
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    public AbstractMap.SimpleEntry<Integer, List<Announcement>> readGeneral(int number) {
        int uuid = UUIDGenerator.generateUUID();
        ProtocolMessage pm = new ProtocolMessage("READGENERAL", _pubKey, uuid, number);
        VerifiableProtocolMessage vpm = requestServer(pm);

        StatusCode rsc = getStatusCodeFromVPM(vpm);
        List<Announcement> announcements = getAnnouncementsFromVPM(vpm);

        return new AbstractMap.SimpleEntry<>(rsc.getCode(), announcements);
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
        return number < 0;
    }

    /**
     * Verifies if a user exists within _usersPubKeys.
     * @param user
     */
    public boolean invalidUser(int user) {
        return user < 0 || user >= _usersPubKeys.size();
    }

}
