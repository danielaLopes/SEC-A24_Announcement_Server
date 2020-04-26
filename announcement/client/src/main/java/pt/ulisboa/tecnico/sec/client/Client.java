package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.net.Socket;
import java.net.SocketTimeoutException;
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
import java.util.Collections;

import javax.crypto.*;

public class Client{

    protected PublicKey _pubKey;
    private PrivateKey _privateKey;

    private List<PublicKey> _usersPubKeys;

    protected static final List<Integer> _serverPorts = Collections.unmodifiableList(
            new ArrayList<Integer>() {{
                add(8005);
                add(8006);
                add(8007);
            }});

    protected static final List<String> _serverPubKeyPaths = Collections.unmodifiableList(
            new ArrayList<String>() {{
                add("../server/src/main/resources/crypto/public.key");
                add("../server/src/main/resources/crypto/public2.key");
                add("../server/src/main/resources/crypto/public3.key");
            }});

    private List<PublicKey> _serverPubKeys;

    protected final Communication _communication;
    protected List<ObjectOutputStream> _oos;
    protected List<ObjectInputStream> _ois;
    private List<Socket> _clientSockets;
    private Socket _clientSocket;

    protected List<String> _tokens;

    protected String _token;

    protected static final int TIMEOUT = 1000;
    protected static final int MAX_REQUESTS = 5;
    protected static final int MAX_REFRESH = 3;

    public Client(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias,
                  List<String> otherUsersPubKeyPaths) {
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);

        //loadServerPublicKey(serverPubKeyPath);
        _serverPubKeys = new ArrayList<>();
        loadServersGroupPublicKeys();

        _usersPubKeys = new ArrayList<PublicKey>();
        loadUsersPubKeys(otherUsersPubKeyPaths);

        _communication = new Communication();

        _oos = new ArrayList<>();
        _ois = new ArrayList<>();
        _clientSockets = new ArrayList<>();

        _tokens = new ArrayList<>();

        //startServerCommunication(0);
        startServersGroupCommunication();
    }

    public Client(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias) {
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);
        
        //loadServerPublicKey(serverPubKeyPath);
        _serverPubKeys = new ArrayList<>();
        loadServersGroupPublicKeys();

        _communication = new Communication();

        _oos = new ArrayList<>();
        _ois = new ArrayList<>();
        _clientSockets = new ArrayList<>();

        _tokens = new ArrayList<>();

        //startServerCommunication(0);
        startServersGroupCommunication();
    }

    /**
     * Loads Client's public key to _pubKey.
     */
    public void loadPublicKey(String pubKeyPath) {
        if (pubKeyPath == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load public key.\n");
            System.exit(-1);
        }
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
        if (keyStorePath == null || keyStorePasswd == null || entryPasswd == null || alias == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load keystore.\n");
            System.exit(-1);
        }

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

    public void loadServersGroupPublicKeys() {
        for (String path : _serverPubKeyPaths) {
            loadServerPublicKey(path);
        }
    }

    /**
     * Loads server's public key to _serverPubKey.
     */
    public void loadServerPublicKey(String path) {
        System.out.println("server pubkey path: " + path);
        System.out.println("list: " + _serverPubKeys);
        if (path == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load server's public key.\n");
            System.exit(-1);
        }

        try {
            //_serverPubKey = KeyPairUtil.loadPublicKey(path);
            _serverPubKeys.add(KeyPairUtil.loadPublicKey(path));
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load server's public key.\n" + e);
            System.exit(-1);
        }
    }

    /**
     * Loads other known user's public keys to _usersPubKeys.
     */
    public void loadUsersPubKeys(List<String> paths) {
        if (paths == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load other users public keys.\n");
            System.exit(-1);
        }
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

    public List<PublicKey> getServersGroupPubKeys() {
        return _serverPubKeys;
    }

    /**
     * @return the server's public key
     */
    public PublicKey getServerPubKey(int serverIndex) {
        return _serverPubKeys.get(serverIndex);
    }

    /**
     * @return the client's public key
     */
    public PublicKey getPubKey() {
        return _pubKey;
    }

    /**
     * Retrieves the public key of a specific user.
     * @param userIndex describes the index of a user in _usersPubKeys
     */
    public PublicKey getPublicKeyFromUser(int userIndex) {
        return _usersPubKeys.get(userIndex);
    }

    /**
     * Starts the communication with the group of active servers for future operations.
     */
    public void startServersGroupCommunication() {
        try {
            Socket socket;
            for (int serverPort : _serverPorts) {
                socket = new Socket("localhost", serverPort);
                socket.setSoTimeout(TIMEOUT);
                _clientSockets.add(socket);

                _oos.add(new ObjectOutputStream(socket.getOutputStream()));
                _ois.add(new ObjectInputStream(socket.getInputStream()));
            }

            registerServersGroup();
        }
        catch(IOException e) {
            System.out.println("Error starting client socket. Make sure the server is running.");
        }
    }

    /**
     * Starts the communication with the server for future operations.
     */
    public void startServerCommunication(int serverIndex) {
        try {
            Socket clientSocket = new Socket("localhost", _serverPorts.get(serverIndex));
            clientSocket.setSoTimeout(TIMEOUT);
            _clientSockets.add(clientSocket);


            _oos.add(new ObjectOutputStream(clientSocket.getOutputStream()));
            _ois.add(new ObjectInputStream(clientSocket.getInputStream()));

            register();
        }
        catch(IOException e) {
            System.out.println("Error starting client socket. Make sure the server is running.");
        }
    }

    /**
     * Closes the communication with the group of active servers.
     */
    public void closeGroupCommunication() {
        try {
            for (int i = 0; i < _serverPorts.size(); i++) {
                ProtocolMessage pm = new ProtocolMessage("LOGOUT");
                VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
                _communication.sendMessage(vpm, _oos.get(i));
                _communication.close(_clientSockets.get(i));
            }
        }
        catch(IOException e) {
            System.out.println("Error closing socket.");
        }
    }

    /**
     * Closes the communication with the server.
     */
    public void closeCommunication(int serverIndex) {
        try {
            ProtocolMessage pm = new ProtocolMessage("LOGOUT");
            VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
            _communication.sendMessage(vpm, _oos.get(serverIndex));
            _communication.close(_clientSockets.get(serverIndex));
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
        if (sc == null) return;

        System.out.println("Status Code: ====== " + sc + ": " + sc.getDescription() + " ======");
    }

    /**
     * Creates a VerifiableProtocolMessage with pm and signed pm, in order
     * to avoid messages being tampered.
     * @param pm is a ProtocolMessage
     * @return the VerificableProtocolMessage to be sent
     */
    public VerifiableProtocolMessage createVerifiableMessage(ProtocolMessage pm) {
        if (pm == null) return null;

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
    public boolean verifySignature(VerifiableProtocolMessage vpm, int serverIndex) {
        if (vpm == null) return false;
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(vpm.getProtocolMessage());
            return SignatureUtil.verifySignature(vpm.getSignedProtocolMessage(), _serverPubKeys.get(serverIndex), bpm);
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
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getStatusCode();
    }

    /**
     * Retrieves the list of announcements of a VerifiableProtocolMessage.
     * @param vpm is a VerifiableProtocolMessage
     * @return list of announcements
     */
    public List<Announcement> getAnnouncementsFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getAnnouncements();
    }

    public byte[] encryptToken(String token, PublicKey serverPubKey) {
        byte[] nextOperationToken = ProtocolMessageConverter.objToByteArray(token); 
        try {
            return SignatureUtil.encrypt(nextOperationToken, serverPubKey);
        }
        catch(InvalidKeyException | NoSuchAlgorithmException | SignatureException |
                NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("Could not encrypt user's token.");
        }
        return null;
    }

    public String decryptToken(byte[] token) {
        try {
            byte[] decryptedToken = SignatureUtil.decrypt(token, _privateKey);
            return (String) ProtocolMessageConverter.byteArrayToObj(decryptedToken);
        }
        catch(InvalidKeyException | NoSuchAlgorithmException | SignatureException |
        NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("Could not decrypt user's token.");
        }
        return null;
    }

    public String getTokenFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return decryptToken(vpm.getProtocolMessage().getToken());
    }

    public String getOldTokenFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return decryptToken(vpm.getProtocolMessage().getOldToken());
    }

    /**
     * Makes a request to the group of active Servers, receiving a list of responses as VerifiableProtocolMessage's
     * @param pm is the ProtocolMessage to be sent
     * @return the servers' responses
     */
    public List<VerifiableProtocolMessage> requestServersGroup(ProtocolMessage pm) {

        List<VerifiableProtocolMessage> responses = new ArrayList<>();
        for (int serverIndex = 0; serverIndex < _serverPorts.size(); serverIndex++) {
            responses.add(requestServer(pm, serverIndex));
        }

        return responses;
    }

    /**
     * Makes a request to the Server, receiving a response as a VerifiableProtocolMessage
     * @param pm is the ProtocolMessage to be sent
     * @return the server's response
     */
    public VerifiableProtocolMessage requestServer(ProtocolMessage pm, int serverIndex) {
        if (pm == null) return null;

        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        VerifiableProtocolMessage rvpm = null;
        StatusCode rsc = null;
        int requestsCounter = 0;

        while (rvpm == null && requestsCounter < MAX_REQUESTS) {
            try {
                _communication.sendMessage(vpm, _oos.get(serverIndex));
                rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois.get(serverIndex));
                if (rvpm == null) {
                    return null;
                }
                rsc = getStatusCodeFromVPM(rvpm);

                if (verifySignature(rvpm, 0)) {
                    System.out.println("Server signature verified successfully");
                    printStatusCode(rsc);
                }
                else {
                    System.out.println("Could not register: could not verify server signature");
                }
            }
            catch(SocketTimeoutException e) {
                System.out.println("Could not receive a response on request " + (++requestsCounter) + 
                ". Trying again...");
            }
            catch (IOException | ClassNotFoundException e) {
                System.out.println(e);
            }
        }

        return rvpm;
    }

    public List<StatusCode> registerServersGroup() {
        ProtocolMessage pm = new ProtocolMessage("REGISTER", _pubKey);
        List<VerifiableProtocolMessage> vpms = requestServersGroup(pm);

        List<StatusCode> rscs = new ArrayList<>();
        int serverIndex = 0;
        for(VerifiableProtocolMessage vpm : vpms) {
            StatusCode rsc = null;
            if (vpm == null) {
                rsc = StatusCode.NO_RESPONSE;
                System.out.println("Could not register: could not receive a response");
                closeCommunication(serverIndex++);
                System.exit(-1);
            }
            else {
                rscs.add(getStatusCodeFromVPM(vpm));
                _tokens.add(getTokenFromVPM(vpm));
            }
        }

        return rscs;
    }

    /**
     * Allows the Client to register in the Server, hence providing its Public Key.
     * Must be the first operation to be done in the Client-Server communication.
     * @return the StatusCode of the operation
     */
    public StatusCode register() {
        ProtocolMessage pm = new ProtocolMessage("REGISTER", _pubKey);
        VerifiableProtocolMessage vpm = requestServer(pm, 0);

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
            System.out.println("Could not register: could not receive a response");
            closeCommunication(0);
            System.exit(-1);
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            _token = getTokenFromVPM(vpm);
        }
        
        return rsc;
    }

    public List<StatusCode> postServersGroup(String message, List<String> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return new ArrayList<StatusCode>(StatusCode.NULL_FIELD);
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return new ArrayList<StatusCode>(StatusCode.NULL_FIELD);
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return new ArrayList<StatusCode>(StatusCode.INVALID_MESSAGE_LENGTH);
        }

        int refreshCounter = 0;
        StatusCode rsc = null;

        while (refreshCounter < MAX_REFRESH) {
            Announcement a = new Announcement(message, references);
            ProtocolMessage pm = new ProtocolMessage("POST", _pubKey, a, encryptToken(_token, _serverPubKeys.get(0)));

            List<VerifiableProtocolMessage> vpms = requestServersGroup(pm);

            List<StatusCode> rscs = new ArrayList<>();

            for(VerifiableProtocolMessage vpm : vpms) {

                rscs.add(verifyReceivedMessage(vpm));
                if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                    refreshToken();
                    refreshCounter++;
                } else {
                    refreshCounter = MAX_REFRESH;
                }
            }
        }

        return rscs;
    }

    /**
     * Posts an announcement to the Client's Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    public StatusCode post(String message, List<String> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return StatusCode.NULL_FIELD;
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return StatusCode.NULL_FIELD;
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return StatusCode.INVALID_MESSAGE_LENGTH;
        }
        
        int refreshCounter = 0;
        StatusCode rsc = null;

        while (refreshCounter < MAX_REFRESH) {
            Announcement a = new Announcement(message, references);
            ProtocolMessage pm = new ProtocolMessage("POST", _pubKey, a, encryptToken(_token, _serverPubKeys.get(0)));
            VerifiableProtocolMessage vpm = requestServer(pm, 0);
            rsc = verifyReceivedMessage(vpm);
            if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                refreshToken();
                refreshCounter++;
            }
            else {
                refreshCounter = MAX_REFRESH;
            }
        }
        
        return rsc;
    }

    /**
     * Posts an announcement to the General Board. This announcement
     * can refer to previous announcements and has a UUID.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    public StatusCode postGeneral(String message, List<String> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return StatusCode.NULL_FIELD;
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return StatusCode.NULL_FIELD;
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return StatusCode.INVALID_MESSAGE_LENGTH;
        }

        Announcement a = new Announcement(message, references);

        int refreshCounter = 0;
        StatusCode rsc = null;
        
        while (refreshCounter < MAX_REFRESH) {
            ProtocolMessage pm = new ProtocolMessage("POSTGENERAL", _pubKey, a, encryptToken(_token, _serverPubKeys.get(0)));
            VerifiableProtocolMessage vpm = requestServer(pm, 0);
            rsc = verifyReceivedMessage(vpm);
            if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                refreshToken();
                refreshCounter++;
            }
            else {
                refreshCounter = MAX_REFRESH;
            }
        }
        
        return rsc;
    }

    /**
     * Retrieves the number latest announcements from the user's Board.
     * @param user public key
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> read(PublicKey user, int number) {
        if (user == null) {
            System.out.println("Invalid user.");
            return new AbstractMap.SimpleEntry<>(StatusCode.NULL_FIELD, new ArrayList<>());
        }

        List<Announcement> announcements = null;
        StatusCode rsc = null;
        VerifiableProtocolMessage vpm = null;

        int refreshCounter = 0;
        while (refreshCounter < MAX_REFRESH) {
            ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, encryptToken(_token, _serverPubKeys.get(0)), number, user);
            vpm = requestServer(pm, 0);
            rsc = verifyReceivedMessage(vpm);
            if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                refreshToken();
                refreshCounter++;
            }
            else {
                refreshCounter = MAX_REFRESH;
            }
        }
        
        if (rsc.equals(StatusCode.OK)) {
            announcements = getAnnouncementsFromVPM(vpm);
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }

    /**
     * Retrieves the number latest announcements from the user's Board.
     * @param user position in _usersPubKeys
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> read(int user, int number) {
        if (invalidUser(user)) {
            System.out.println("Invalid user.");
            return new AbstractMap.SimpleEntry<>(StatusCode.USER_NOT_REGISTERED, new ArrayList<>());
        }
        PublicKey userToReadPB = _usersPubKeys.get(user);
        
        List<Announcement> announcements = null;
        StatusCode rsc = null;
        VerifiableProtocolMessage vpm = null;

        int refreshCounter = 0;
        while (refreshCounter < MAX_REFRESH) {
            ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, encryptToken(_token, _serverPubKeys.get(0)), number, userToReadPB);
            vpm = requestServer(pm, 0);
            rsc = verifyReceivedMessage(vpm);
            if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                refreshToken();
                refreshCounter++;
            }
            else {
                refreshCounter = MAX_REFRESH;
            }
        }
        
        if (rsc.equals(StatusCode.OK)) {
            announcements = getAnnouncementsFromVPM(vpm);
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }

    /**
     * Retrieves the number latest announcements from the General Board.
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readGeneral(int number) {
        List<Announcement> announcements = null;
        StatusCode rsc = null;
        VerifiableProtocolMessage vpm = null;
        
        int refreshCounter = 0;
        while (refreshCounter < MAX_REFRESH) {
            ProtocolMessage pm = new ProtocolMessage("READGENERAL", _pubKey, encryptToken(_token, _serverPubKeys.get(0)), number);
            vpm = requestServer(pm, 0);
            rsc = verifyReceivedMessage(vpm);
            if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                refreshToken();
                refreshCounter++;
            }
            else {
                refreshCounter = MAX_REFRESH;
            }
        }
        
        if (rsc.equals(StatusCode.OK)) {
            announcements = getAnnouncementsFromVPM(vpm);
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }

    public StatusCode refreshToken() {
        ProtocolMessage pm = new ProtocolMessage("TOKEN", _pubKey);
        VerifiableProtocolMessage vpm = requestServer(pm, 0);
        
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
            System.out.println("Could not refresh token: could not receive a response");
            closeCommunication(0);
            System.exit(-1);
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            _token = getTokenFromVPM(vpm);
        }
        
        return rsc;
    }

    /**
     * Verifies if a message has valid length.
     * @param message
     */
    public boolean invalidMessageLength(String message) {
        if (message == null) return false;
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

    public boolean invalidToken(String token) {
        return !token.equals(_token);
    }

    public StatusCode verifyReceivedMessage(VerifiableProtocolMessage vpm) {
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            if (invalidToken(getOldTokenFromVPM(vpm))) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        return rsc;
    }

}
