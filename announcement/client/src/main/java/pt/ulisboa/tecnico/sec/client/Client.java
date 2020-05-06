package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;
import pt.ulisboa.tecnico.sec.crypto_lib.ProtocolMessageConverter;
import pt.ulisboa.tecnico.sec.crypto_lib.SignatureUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.net.Socket;
import java.net.SocketException;
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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

import javax.crypto.*;

public class Client {

    protected PublicKey _pubKey;
    private PrivateKey _privateKey;

    protected List<PublicKey> _usersPubKeys;

    protected final Integer _startingServerPort = 8001;

    protected final String _serverPubKeyPrefix = "../server/src/main/resources/crypto/public";
    protected final String _serverPubKeySufix = ".key";

    protected final int _nServers;

    // maps server public keys to the corresponding serverIndex to access for _oos,_ois or _clientSockets
    private Map<PublicKey, CommunicationServer> _serverCommunications;
    // TODO: change this
    private PublicKey _serverPubKey;

    private ClientUI _clientUI;

    protected final Communication _communication;

    private AtomicRegister1N _atomicRegister1N;
    /*protected List<ObjectOutputStream> _oos;
    protected List<ObjectInputStream> _ois;
    private List<Socket> _clientSockets;*/

    // maps server's public key to corresponding token
    //protected Map<PublicKey, String> _tokens;

    //protected String _token;

    Integer _acks = 0;

    protected static final int TIMEOUT = 500000;
    protected static final int MAX_REQUESTS = 5;
    protected static final int MAX_REFRESH = 3;

    public Client(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias,
                  int nServers, List<String> otherUsersPubKeyPaths, ClientUI clientUI) {
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);

        //loadServerPublicKey(serverPubKeyPath);
        _nServers = nServers;
        _serverCommunications = new HashMap<>();
        List<PublicKey> serversPubKeys = loadServersGroupPublicKeys();

        _usersPubKeys = new ArrayList<>();
        loadUsersPubKeys(otherUsersPubKeyPaths);

        _communication = new Communication();

        //startServerCommunication(0);
        startServersGroupCommunication(serversPubKeys);

        _atomicRegister1N = new AtomicRegister1N(this);
        
        _clientUI = clientUI;

    }

    public Client(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias, int nServers) {
        loadPublicKey(pubKeyPath);
        loadPrivateKey(keyStorePath, keyStorePasswd, entryPasswd, alias);
        
        //loadServerPublicKey(serverPubKeyPath);
        _nServers = nServers;
        _serverCommunications = new HashMap<>();
        List<PublicKey> serversPubKeys = loadServersGroupPublicKeys();

        _communication = new Communication();


        //startServerCommunication(0);
        startServersGroupCommunication(serversPubKeys);

        _atomicRegister1N = new AtomicRegister1N(this);
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

    public List<PublicKey> loadServersGroupPublicKeys() {
        List<PublicKey> serverPublicKeys = new ArrayList<>();
        for (int i = 1; i <= _nServers; i++) {
            serverPublicKeys.add(loadServerPublicKey(_serverPubKeyPrefix + i + _serverPubKeySufix));
        }
        return serverPublicKeys;
    }

    /**
     * Loads server's public key to _serverPubKey.
     */
    public PublicKey loadServerPublicKey(String path) {
        PublicKey serverPubKey = null;
        if (path == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load server's public key.\n");
            System.exit(-1);
        }

        try {
            //_serverPubKey = KeyPairUtil.loadPublicKey(path);
            serverPubKey = KeyPairUtil.loadPublicKey(path);
            // TODO: change this
            // _serverPubKey = serverPubKey;

            //_serverPubKeys.put(serverPubKey, serverIndex);
        } catch (Exception e) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load server's public key.\n" + e);
            System.exit(-1);
        }
        return serverPubKey;
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
        return new ArrayList<>(_serverCommunications.keySet());
    }

    /**
     * @return the server's public key
     */
    /*public PublicKey getServerPubKey(int serverIndex) {
        for ()
        return _serverPubKeys.get(serverIndex);
    }*/

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

    public CommunicationServer createServerCommunication(int port) throws IOException {

        Socket socket = new Socket("localhost", port);
        socket.setSoTimeout(TIMEOUT);

        return new CommunicationServer(
                port, new ObjectOutputStream(socket.getOutputStream()),
                new ObjectInputStream(socket.getInputStream()), socket);
    }

    /**
     * Starts the communication with the group of active servers for future operations.
     */
    public void startServersGroupCommunication(List<PublicKey> serverPublicKeys) {
        try {
            int serverIndex = 0;
            for (PublicKey serverPubKey : serverPublicKeys) {
                int port = _startingServerPort + serverIndex++;
                System.out.println("Connecting with server at port " + port);

                _serverCommunications.put(serverPubKey, createServerCommunication(port));
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
    /*public void startServerCommunication(int serverIndex) {
        try {
            Socket clientSocket = new Socket("localhost", _startingServerPort + serverIndex);
            clientSocket.setSoTimeout(TIMEOUT);
            _clientSockets.add(clientSocket);


            _oos.add(new ObjectOutputStream(clientSocket.getOutputStream()));
            _ois.add(new ObjectInputStream(clientSocket.getInputStream()));

            register();
        }
        catch(IOException e) {
            System.out.println("Error starting client socket. Make sure the server is running.");
        }
    }*/

    /**
     * Closes the communication with the group of active servers.
     */
    public void closeGroupCommunication() {
        for (CommunicationServer serverCommunication : _serverCommunications.values()) {
            closeCommunication(serverCommunication);
        }
    }

    /**
     * Closes the communication with the server.
     */
    public void closeCommunication(CommunicationServer serverCommunication) {
        try {
            ProtocolMessage pm = new ProtocolMessage("LOGOUT");
            VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
            _communication.sendMessage(vpm, serverCommunication.getObjOutStream());
            _communication.close(serverCommunication.getClientSocket());
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
    public boolean verifySignature(VerifiableProtocolMessage vpm, PublicKey serverPubKey) {
        if (vpm == null) return false;
        try {
            byte[] bpm = ProtocolMessageConverter.objToByteArray(vpm.getProtocolMessage());
            return SignatureUtil.verifySignature(vpm.getSignedProtocolMessage(), serverPubKey, bpm);
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

    public PublicKey getServerPublicKeyFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getPublicKey();
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

    public String getTokenFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getToken();
    }

    public String getOldTokenFromVPM(VerifiableProtocolMessage vpm) {
        if (vpm == null) return null;
        return vpm.getProtocolMessage().getOldToken();
    }

    /**
     * Makes a request to the group of active Servers to register and to obtain tokens,
     * receiving a list of responses as VerifiableProtocolMessage's
     * @param pm is a ProtocolMessage to be sent to all servers in register phase, when there's no token associated.
     * @return the servers' responses
     */
    public Map<PublicKey, VerifiableProtocolMessage> requestServersGroupRegister(ProtocolMessage pm) {

        Map<PublicKey, VerifiableProtocolMessage> responses = new HashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
            // TODO: possible attack: server sending wrong public key?????
            responses.put(entry.getKey(), requestServer(pm, entry.getValue()));
        }

        return responses;
    }

    /**
     * Makes a request to the group of active Servers, receiving a list of responses as VerifiableProtocolMessage's
     * @param pms is a List the ProtocolMessages to be sent to each server (each server has a different token)
     * @return the servers' responses
     */
    public Map<PublicKey, VerifiableProtocolMessage> requestServersGroup(Map<PublicKey, ProtocolMessage> pms) {
        Map<PublicKey, VerifiableProtocolMessage> responses = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, ProtocolMessage> pm : pms.entrySet()) {
            /*String opUuid = UUIDGenerator.generateUUID();
            pm.getValue().setOpUuid(opUuid);*/
            Thread thread = new Thread(){
                public void run() {
                    VerifiableProtocolMessage response = requestServer(pm.getValue(), _serverCommunications.get(pm.getKey()));
                    if (response != null) {
                        responses.put(pm.getKey(), response);
                    }
                }
            };
            thread.start();
        }
        // TODO: change number of responses
        while (responses.size() < _nServers);
        System.out.println("got out");
        return responses;
    }

    public void deliverPost() {
        if (_clientUI != null)
            _clientUI.deliverPost(StatusCode.OK);
        
    }

    public void deliverRead(List<Announcement> announcements) {
        if (_clientUI != null)
            _clientUI.deliverRead(StatusCode.OK, announcements);
        
    }

    public void write(Map<PublicKey, ProtocolMessage> pms) {
        System.out.println("write");
        Map<PublicKey, VerifiableProtocolMessage> responses = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, ProtocolMessage> pm : pms.entrySet()) {
            /*String opUuid = UUIDGenerator.generateUUID();
            pm.getValue().setOpUuid(opUuid);*/
            Thread thread = new Thread(){
                public void run() {
                    VerifiableProtocolMessage response = requestServer(pm.getValue(), _serverCommunications.get(pm.getKey()));
                    if (response != null && verifyReceivedMessage(response) == StatusCode.OK) {
                        //TODO CHECK HOW MANY SERVERS NEED TO CALL WRITE RETURN
                        _atomicRegister1N.writeReturn(response.getProtocolMessage().getAtomicRegisterMessages().getRid());
                        responses.put(pm.getKey(), response);
                        printStatusCode(response.getProtocolMessage().getStatusCode());
                    }
                }
            };
            thread.start();
        }
    }

    public void writeBack(AtomicRegisterMessages arm) {
        Map<PublicKey, ProtocolMessage> pms = new HashMap<>();
            for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
                ProtocolMessage p = new ProtocolMessage("WRITEBACK", _pubKey, entry.getValue().getToken());
                p.setAtomicRegisterMessages(arm);
                pms.put(entry.getKey(), p);
            }
        write(pms);
    }

    public void readAnnouncements(Map<PublicKey, ProtocolMessage> pms) {
        Map<PublicKey, VerifiableProtocolMessage> responses = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, ProtocolMessage> pm : pms.entrySet()) {
            /*String opUuid = UUIDGenerator.generateUUID();
            pm.getValue().setOpUuid(opUuid);*/
            Thread thread = new Thread(){
                public void run() {
                    VerifiableProtocolMessage response = requestServer(pm.getValue(), _serverCommunications.get(pm.getKey()));
                    if (response != null && response.getProtocolMessage().getCommand().equals("VALUE") && verifyReceivedMessage(response) == StatusCode.OK) {
                        //TODO CHECK HOW MANY SERVERS NEED TO CALL WRITE RETURN
                        _atomicRegister1N.writeBack(response.getProtocolMessage());
                        responses.put(pm.getKey(), response);
                        printStatusCode(response.getProtocolMessage().getStatusCode());
                    }
                }
            };
            thread.start();
        }
    }

    /**
     * Makes a request to the Server, receiving a response as a VerifiableProtocolMessage
     * @param pm is the ProtocolMessage to be sent
     * @return the server's response
     */
    public VerifiableProtocolMessage requestServer(ProtocolMessage pm, CommunicationServer serverCommunication) {
        if (pm == null) return null;

        try {
            // tries to reconnect with dead servers
            if (serverCommunication.getAlive() == false) {
                CommunicationServer newServerCommunication = createServerCommunication(serverCommunication.getPort());
                serverCommunication = newServerCommunication;
            }
        }
        catch (IOException e) {
            System.out.println("Error reconnecting with server at port " + serverCommunication.getPort() +
                    ". Server is still dead!");
            // does not try to send message if server is still dead
            return null;
        }

        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        VerifiableProtocolMessage rvpm = null;
        // TODO: see if status code is required
        //StatusCode rsc = null;
        int requestsCounter = 0;

        while (rvpm == null && requestsCounter < MAX_REQUESTS) {
            try {
                _communication.sendMessage(vpm, serverCommunication.getObjOutStream());
                rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(serverCommunication.getObjInStream());
                if (rvpm == null) {
                    return null;
                }
                //rsc = getStatusCodeFromVPM(rvpm);
                PublicKey serverPubKey = getServerPublicKeyFromVPM(rvpm);

                if (verifySignature(rvpm, serverPubKey)) {
                    System.out.println("Server signature verified successfully");
                    //printStatusCode(rsc);
                }
                else {
                    System.out.println("Could not register: could not verify server signature");
                }

            }
            catch(SocketTimeoutException e) {
                System.out.println("Could not receive a response on request " + (++requestsCounter) + 
                ". Trying again...");
            }
            catch (SocketException e) {
                System.out.println("A server is dead!");
                serverCommunication.setAlive(false);
                return null;
            }
            catch (IOException | ClassNotFoundException e) {
                System.out.println(e);
                System.exit(-1);
            }
        }

        return rvpm;
    }

    public List<StatusCode> registerServersGroup() {
        ProtocolMessage pm = new ProtocolMessage("REGISTER", _pubKey);
        Map<PublicKey, VerifiableProtocolMessage> vpms = requestServersGroupRegister(pm);

        // TODO; decide between list or hashmap
        List<StatusCode> rscs = new ArrayList<>();
        for(Map.Entry<PublicKey, VerifiableProtocolMessage> vpm : vpms.entrySet()) {
            CommunicationServer serverCommunication = _serverCommunications.get(vpm.getKey());

            if (vpm.getValue() == null) {
                rscs.add(StatusCode.NO_RESPONSE);
                // TODO: assume that if a client can't register with one of the servers, client shuts down
                System.out.println("Could not register: could not receive a response");
                closeGroupCommunication();
                System.exit(-1);
            }
            else {
                rscs.add(getStatusCodeFromVPM(vpm.getValue()));
                serverCommunication.setToken(getTokenFromVPM(vpm.getValue()));
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
        System.out.println("going to call requests server from register");

        CommunicationServer serverCommunication = _serverCommunications.get(_serverPubKey);
        VerifiableProtocolMessage vpm = requestServer(pm, serverCommunication);

        StatusCode rsc = null;
        if (vpm == null) {
            System.out.println("Could not register: could not receive a response");
            closeCommunication(serverCommunication);
            System.exit(-1);
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            serverCommunication.setToken(getTokenFromVPM(vpm));
        }
        
        return rsc;
    }

    public List<StatusCode> post(String message, List<String> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.NULL_FIELD));
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.NULL_FIELD));
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.INVALID_MESSAGE_LENGTH));
        }

        int refreshCounter = 0;
        StatusCode rsc;

        List<StatusCode> rscs = new ArrayList<>();

        // TODO: fix this -> loop!
        //while (refreshCounter < MAX_REFRESH) {
            Announcement a = new Announcement(message, references);

            _atomicRegister1N.write();
            int rid = _atomicRegister1N.getRid();
            int wts = _atomicRegister1N.getWts();
            List<Announcement> values = new ArrayList<Announcement>(Arrays.asList(a));

            AtomicRegisterMessages arm = new AtomicRegisterMessages(rid, wts, values);
 
            Map<PublicKey, ProtocolMessage> pms = new HashMap<>();
            for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
                ProtocolMessage p = new ProtocolMessage("POST", _pubKey, a, entry.getValue().getToken());
                p.setAtomicRegisterMessages(arm);
                pms.put(entry.getKey(), p);
            }

            write(pms);

            /*for(Map.Entry<PublicKey, VerifiableProtocolMessage> vpm : vpms.entrySet()) {

                rsc = verifyReceivedMessage(vpm.getValue()); // returns StatusCode.NO_RESPONSE if vpm is null
                rscs.add(rsc);
                if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                    refreshToken(_serverCommunications.get(vpm.getKey()));
                    refreshCounter++;
                } else {
                    refreshCounter = MAX_REFRESH;
                }
            }*/

        return rscs;
    }

    public List<StatusCode> postGeneralServersGroup(String message, List<String> references) {
        if (message == null) {
            System.out.println("Message cannot be null.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.NULL_FIELD));
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            return new ArrayList<StatusCode>(Arrays.asList(StatusCode.NULL_FIELD));
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            return new ArrayList<>(Arrays.asList(StatusCode.INVALID_MESSAGE_LENGTH));
        }

        Announcement a = new Announcement(message, references);

        int refreshCounter = 0;
        StatusCode rsc;

        List<StatusCode> rscs = new ArrayList<>();

        while (refreshCounter < MAX_REFRESH) {

            Map<PublicKey, ProtocolMessage> pms = new HashMap<>();
            for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
                pms.put(entry.getKey(), new ProtocolMessage("POSTGENERAL", _pubKey, a,
                        entry.getValue().getToken()));
            }

            Map<PublicKey, VerifiableProtocolMessage> vpms = requestServersGroup(pms);

            for(Map.Entry<PublicKey, VerifiableProtocolMessage> vpm : vpms.entrySet()) {
                rsc = verifyReceivedMessage(vpm.getValue());
                rscs.add(rsc);
                if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                    refreshToken(_serverCommunications.get(vpm.getKey()));
                    refreshCounter++;
                } else {
                    refreshCounter = MAX_REFRESH;
                }
            }
        }
        return rscs;
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

        CommunicationServer serverCommunication = _serverCommunications.get(_serverPubKey);
        while (refreshCounter < MAX_REFRESH) {
            ProtocolMessage pm = new ProtocolMessage("POSTGENERAL", _pubKey, a,
                    serverCommunication.getToken());
            VerifiableProtocolMessage vpm = requestServer(pm, serverCommunication);
            rsc = verifyReceivedMessage(vpm);
            if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                refreshToken(serverCommunication);
                refreshCounter++;
            }
            else {
                refreshCounter = MAX_REFRESH;
            }
        }
        
        return rsc;
    }

    public List<AbstractMap.SimpleEntry<StatusCode, List<Announcement>>> read(PublicKey user, int number) {
        // TODO: see best way to verify if all servers responses have consensus
        List<AbstractMap.SimpleEntry<StatusCode, List<Announcement>>> announcementsPerServer = new ArrayList<>();
        if (user == null) {
            System.out.println("Invalid user.");
            announcementsPerServer.add(new AbstractMap.SimpleEntry<>(StatusCode.NULL_FIELD, new ArrayList<>()));
        }

        List<Announcement> announcements = null;
        StatusCode rsc = null;

        AtomicRegisterMessages arm = _atomicRegister1N.read();

        Map<PublicKey, ProtocolMessage> pms = new HashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
            ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, entry.getValue().getToken(), number, user);
            pm.setAtomicRegisterMessages(arm);
            pms.put(entry.getKey(), pm);
        }
            
        readAnnouncements(pms);            

        return announcementsPerServer;
    }

    public List<AbstractMap.SimpleEntry<StatusCode, List<Announcement>>> readGeneralServersGroup(int number) {
        List<Announcement> announcements = null;
        StatusCode rsc = null;

        // TODO: see best way to verify if all servers responses have consensus
        List<AbstractMap.SimpleEntry<StatusCode, List<Announcement>>> announcementsPerServer = new ArrayList<>();

        int refreshCounter = 0;
        while (refreshCounter < MAX_REFRESH) {

            Map<PublicKey, ProtocolMessage> pms = new HashMap<>();
            for (Map.Entry<PublicKey, CommunicationServer> entry : _serverCommunications.entrySet()) {
                pms.put(entry.getKey(), new ProtocolMessage("READGENERAL", _pubKey,
                        entry.getValue().getToken(), number));
            }

            Map<PublicKey, VerifiableProtocolMessage> vpms = requestServersGroup(pms);

            for(Map.Entry<PublicKey, VerifiableProtocolMessage> vpm : vpms.entrySet()) {

                rsc = verifyReceivedMessage(vpm.getValue());
                if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                    refreshToken(_serverCommunications.get(vpm.getKey()));
                    refreshCounter++;
                } else {
                    refreshCounter = MAX_REFRESH;
                }
                if (rsc.equals(StatusCode.OK)) {
                    announcements = getAnnouncementsFromVPM(vpm.getValue());
                }
                announcementsPerServer.add(new AbstractMap.SimpleEntry<>(rsc, announcements));
            }
        }

        return announcementsPerServer;
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

        CommunicationServer serverCommunication = _serverCommunications.get(_serverPubKey);
        int refreshCounter = 0;
        while (refreshCounter < MAX_REFRESH) {
            ProtocolMessage pm = new ProtocolMessage("READGENERAL", _pubKey,
                    serverCommunication.getToken(), number);
            System.out.println("going to call requests server from readgeneral");
            vpm = requestServer(pm, serverCommunication);
            rsc = verifyReceivedMessage(vpm);
            if (rsc.equals(StatusCode.INVALID_TOKEN)) {
                refreshToken(serverCommunication);
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

    public StatusCode refreshToken(CommunicationServer serverCommunication) {
        ProtocolMessage pm = new ProtocolMessage("TOKEN", _pubKey);
        System.out.println("going to call requests server from refreshtoken");
        VerifiableProtocolMessage vpm = requestServer(pm, serverCommunication);

        StatusCode rsc = null;
        if (vpm == null) {
            System.out.println("Could not refresh token: could not receive a response");
            return StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            serverCommunication.setToken(getTokenFromVPM(vpm));
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

    public boolean invalidToken(String token, PublicKey serverPubKey) {
        return !token.equals(_serverCommunications.get(serverPubKey).getToken());
    }

    public StatusCode verifyReceivedMessage(VerifiableProtocolMessage vpm) {
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            PublicKey serverPubKey = getServerPublicKeyFromVPM(vpm);

            if (invalidToken(getOldTokenFromVPM(vpm), serverPubKey)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                //_token = getTokenFromVPM(vpm);
                _serverCommunications.get(serverPubKey).setToken(getTokenFromVPM(vpm));
            }
        }
        return rsc;
    }

    public Map<PublicKey, CommunicationServer> getServerCommunications() { return _serverCommunications; }

}
