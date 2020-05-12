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

public class ClientTest extends Client {

    public ClientTest(String pubKeyPath, String keyStorePath,
                  String keyStorePasswd, String entryPasswd, String alias, int nServers, int nFaults) {
        super(pubKeyPath, keyStorePath, keyStorePasswd, entryPasswd, alias, nServers, nFaults);
    }
  
    // ---------------------------------------------------------------------
    // ------------------- METHODS USED FOR TESTING ONLY -------------------
    // ---------------------------------------------------------------------

    /**
     * Makes a request to the Server that has been tampered.
     * @param pm is the ProtocolMessage to be sent
     * @return the server's response
     */
    /*public VerifiableProtocolMessage requestServerTampered(ProtocolMessage pm) {
        if (pm == null) return null;

        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        ProtocolMessage pmTampered = new ProtocolMessage("POST", _pubKey, new Announcement("tampered", new ArrayList<>()), encryptToken(_token, _serverPubKey));
        vpm.setProtocolMessage(pmTampered);

        VerifiableProtocolMessage rvpm = null;
        StatusCode rsc = null;
        int requestsCounter = 0;

        while (rvpm == null && requestsCounter < MAX_REQUESTS) {
            try {
                _communication.sendMessage(vpm, _oos);
                rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);
                if (rvpm == null) {
                    return null;
                }
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
            catch(SocketTimeoutException e) {
                System.out.println("Could not receive a response on request " + (++requestsCounter) + 
                ". Trying again...");
            }
            catch (IOException | ClassNotFoundException e) {
                System.out.println(e);
            }
        }

        return rvpm;
    }*/

    /**
     * Makes a request to the Server. The response has been dropped.
     * In the original method, if a response from the server is not received
     * a SocketTimeoutException is thrown. In this case, we simulate a dropped
     * response by throwing this Exception voluntarily.
     * @param pm is the ProtocolMessage to be sent
     * @return the server's response
     */
    /*public VerifiableProtocolMessage requestServerDropped(ProtocolMessage pm) {
        if (pm == null) return null;

        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        
        VerifiableProtocolMessage rvpm = null;
        int requestsCounter = 0;

        while (rvpm == null && requestsCounter < MAX_REQUESTS) {
            try {
                _communication.sendMessage(vpm, _oos);
                // in the original method, if a message is not received
                // a SocketTimeoutException is thrown
                throw new SocketTimeoutException();
            }
            catch(SocketTimeoutException e) {
                System.out.println("Could not receive a response on request " + (++requestsCounter) + 
                ". Trying again...");
            }
            catch (IOException e) {
                System.out.println(e);
            }
        }

        return rvpm;
    }*/

    /**
     * Makes a request to the Server. The response is null.
     * @param pm is the ProtocolMessage to be sent
     * @return the server's response
     */
    /*public VerifiableProtocolMessage requestServerNull(ProtocolMessage pm) {
        if (pm == null) return null;

        VerifiableProtocolMessage vpm = createVerifiableMessage(pm);
        
        VerifiableProtocolMessage rvpm = null;
        int requestsCounter = 0;

        while (rvpm == null && requestsCounter < MAX_REQUESTS) {
            try {
                _communication.sendMessage(vpm, _oos);
                rvpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);
                rvpm = null;
                if (rvpm == null) {
                    return null;
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
    }*/

    /**
     * Posts an announcement to the Client's Board twice.
     * @param message to be announced
     * @param references to previous announcements
     * @return a list of the StatusCode of the two consecutive operations
     */
    /*public List<StatusCode> postTwice(String message, List<String> references) {
        List<StatusCode> rsc = new ArrayList<StatusCode>();        
        if (message == null) {
            System.out.println("Message cannot be null.");
            rsc.add(StatusCode.NULL_FIELD);
            return rsc;
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            rsc.add(StatusCode.NULL_FIELD);
            return rsc;
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            rsc.add(StatusCode.INVALID_MESSAGE_LENGTH);
            return rsc;
        }

        Announcement a = new Announcement(message, references);

        ProtocolMessage pm = new ProtocolMessage("POST", _pubKey, a, encryptToken(_token, _serverPubKey));
        for (int i = 0; i < 2; i ++) {
            VerifiableProtocolMessage vpm = requestServer(pm);        

            if (vpm == null) {
                rsc.add(StatusCode.NO_RESPONSE);
            }
            else {
                rsc.add(getStatusCodeFromVPM(vpm));
                System.out.println("old token: " + getOldTokenFromVPM(vpm));
                System.out.println("_token: " + _token);
                if (!getOldTokenFromVPM(vpm).equals(_token)) {
                    rsc.add(StatusCode.INVALID_TOKEN);
                }
                else {
                    _token = getTokenFromVPM(vpm);
                }
            }
        }
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the Client's Board.
     * The request has been tampered.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    /*public StatusCode postTampered(String message, List<String> references) {
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
        ProtocolMessage pm = new ProtocolMessage("POST", _pubKey, a, encryptToken(_token, _serverPubKey));
        VerifiableProtocolMessage vpm = requestServerTampered(pm);
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the Client's Board.
     * The response has been dropped.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    /*public StatusCode postDropped(String message, List<String> references) {
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
        ProtocolMessage pm = new ProtocolMessage("POST", _pubKey, a, encryptToken(_token, _serverPubKey));
        VerifiableProtocolMessage vpm = requestServerDropped(pm);
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the Client's Board. 
     ª The response is null.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    /*public StatusCode postNull(String message, List<String> references) {
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
        ProtocolMessage pm = new ProtocolMessage("POST", _pubKey, a, encryptToken(_token, _serverPubKey));
        VerifiableProtocolMessage vpm = requestServerNull(pm);
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the Client's Board.
     * Sends an invalid postGeneral request to the Server.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    /*public StatusCode postInvalid(String message, List<String> references) {
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
        ProtocolMessage pm = new ProtocolMessage("POSTS", _pubKey, a, encryptToken(_token, _serverPubKey));
        VerifiableProtocolMessage vpm = requestServer(pm);
        System.out.println("lalala");
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (getOldTokenFromVPM(vpm) == null || !getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the Client's Board twice.
     * @param message to be announced
     * @param references to previous announcements
     * @return a list of the StatusCode of the two consecutive operations
     */
    /*public List<StatusCode> postGeneralTwice(String message, List<String> references) {
        List<StatusCode> rsc = new ArrayList<StatusCode>();
        
        if (message == null) {
            System.out.println("Message cannot be null.");
            rsc.add(StatusCode.NULL_FIELD);
            return rsc;
        }
        if (references == null) {
            System.out.println("References cannot be null.");
            rsc.add(StatusCode.NULL_FIELD);
            return rsc;
        }
        if (invalidMessageLength(message)) {
            System.out.println("Maximum message length to post announcement is 255.");
            rsc.add(StatusCode.INVALID_MESSAGE_LENGTH);
            return rsc;
        }

        Announcement a = new Announcement(message, references);
        ProtocolMessage pm = new ProtocolMessage("POSTGENERAL", _pubKey, a, encryptToken(_token, _serverPubKey));
        for (int i = 0; i < 2; i ++) {
            VerifiableProtocolMessage vpm = requestServer(pm);        

            if (vpm == null) {
                rsc.add(StatusCode.NO_RESPONSE);
            }
            else {
                rsc.add(getStatusCodeFromVPM(vpm));
                System.out.println("old token: " + getOldTokenFromVPM(vpm));
                System.out.println("_token: " + _token);
                if (!getOldTokenFromVPM(vpm).equals(_token)) {
                    rsc.add(StatusCode.INVALID_TOKEN);
                }
                else {
                    _token = getTokenFromVPM(vpm);
                }
            }
        }

        for (StatusCode sc : rsc) System.out.println(sc.getDescription());
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the Client's Board.
     * The request has been tampered.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    /*public StatusCode postGeneralTampered(String message, List<String> references) {
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
        ProtocolMessage pm = new ProtocolMessage("POSTGENERAL", _pubKey, a, encryptToken(_token, _serverPubKey));
        VerifiableProtocolMessage vpm = requestServerTampered(pm);
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the Client's Board.
     * The response has been dropped.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    /*public StatusCode postGeneralDropped(String message, List<String> references) {
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
        ProtocolMessage pm = new ProtocolMessage("POSTGENERAL", _pubKey, a, encryptToken(_token, _serverPubKey));
        VerifiableProtocolMessage vpm = requestServerDropped(pm);
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the Client's Board.
     * The response is null.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    /*public StatusCode postGeneralNull(String message, List<String> references) {
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
        ProtocolMessage pm = new ProtocolMessage("POSTGENERAL", _pubKey, a, encryptToken(_token, _serverPubKey));
        VerifiableProtocolMessage vpm = requestServerNull(pm);
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        
        return rsc;
    }*/

    /**
     * Posts an announcement to the General Board.
     * Sends an invalid postGeneral request to the Server.
     * @param message to be announced
     * @param references to previous announcements
     * @return the StatusCode of the operation
     */
    /*public StatusCode postGeneralInvalid(String message, List<String> references) {
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
        ProtocolMessage pm = new ProtocolMessage("POSTSGENERAL", _pubKey, a, encryptToken(_token, _serverPubKey));
        VerifiableProtocolMessage vpm = requestServer(pm);
        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
            }
        }
        
        return rsc;
    }*/

    /**
     * Retrieves the number latest announcements from the user's Board twice.
     * @param user public key
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<List<StatusCode>, List<Announcement>> readTwice(PublicKey user, int number) {
        List<StatusCode> rsc = new ArrayList<StatusCode>();
        List<Announcement> announcements = new ArrayList<>();

        if (user == null) {
            System.out.println("Invalid user.");
            
            rsc.add(StatusCode.NULL_FIELD);
            return new AbstractMap.SimpleEntry<>(rsc, announcements);
        }
        ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, encryptToken(_token, _serverPubKey), number, user);
        for (int i = 0; i < 2; i ++) {
            VerifiableProtocolMessage vpm = requestServer(pm);        

            if (vpm == null) {
                rsc.add(StatusCode.NO_RESPONSE);
            }
            else {
                rsc.add(getStatusCodeFromVPM(vpm));
                System.out.println("old token: " + getOldTokenFromVPM(vpm));
                System.out.println("_token: " + _token);
                if (!getOldTokenFromVPM(vpm).equals(_token)) {
                    rsc.add(StatusCode.INVALID_TOKEN);
                }
                else {
                    _token = getTokenFromVPM(vpm);
                }
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/
    
    /**
     * Retrieves the number latest announcements from the user's Board.
     * The request is tampered.
     * @param user public key
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readTampered(PublicKey user, int number) {
        if (user == null) {
            System.out.println("Invalid user.");
            return new AbstractMap.SimpleEntry<>(StatusCode.NULL_FIELD, new ArrayList<>());
        }
        ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, encryptToken(_token, _serverPubKey), number, user);
        VerifiableProtocolMessage vpm = requestServerTampered(pm);
        List<Announcement> announcements = null;

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
                announcements = getAnnouncementsFromVPM(vpm);
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/

    /**
     * Retrieves the number latest announcements from the user's Board.
     * The response was dropped.
     * @param user public key
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readDropped(PublicKey user, int number) {
        if (user == null) {
            System.out.println("Invalid user.");
            return new AbstractMap.SimpleEntry<>(StatusCode.NULL_FIELD, new ArrayList<>());
        }
        ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, encryptToken(_token, _serverPubKey), number, user);
        VerifiableProtocolMessage vpm = requestServerDropped(pm);
        List<Announcement> announcements = null;

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
                announcements = getAnnouncementsFromVPM(vpm);
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/

    /**
     * Retrieves the number latest announcements from the user's Board.
     * The response is null.
     * @param user public key
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readNull(PublicKey user, int number) {
        if (user == null) {
            System.out.println("Invalid user.");
            return new AbstractMap.SimpleEntry<>(StatusCode.NULL_FIELD, new ArrayList<>());
        }
        ProtocolMessage pm = new ProtocolMessage("READ", _pubKey, encryptToken(_token, _serverPubKey), number, user);
        VerifiableProtocolMessage vpm = requestServerNull(pm);
        List<Announcement> announcements = null;

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
                announcements = getAnnouncementsFromVPM(vpm);
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/

    /**
     * Retrieves the number latest announcements from the user's Board.
     * Sends an invalid readGeneral request to the Server.
     * @param user public key
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readInvalid(PublicKey user, int number) {
        if (user == null) {
            System.out.println("Invalid user.");
            return new AbstractMap.SimpleEntry<>(StatusCode.NULL_FIELD, new ArrayList<>());
        }
        ProtocolMessage pm = new ProtocolMessage("READS", _pubKey, encryptToken(_token, _serverPubKey), number, user);
        VerifiableProtocolMessage vpm = requestServer(pm);
        List<Announcement> announcements = null;

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
                announcements = getAnnouncementsFromVPM(vpm);
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/

    /**
     * Retrieves the number latest announcements from the General Board twice.
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<List<StatusCode>, List<Announcement>> readGeneralTwice(int number) {
        List<StatusCode> rsc = new ArrayList<StatusCode>();
        List<Announcement> announcements = new ArrayList<>();

        ProtocolMessage pm = new ProtocolMessage("READGENERAL", _pubKey, encryptToken(_token, _serverPubKey), number);
        for (int i = 0; i < 2; i ++) {
            VerifiableProtocolMessage vpm = requestServer(pm);        

            if (vpm == null) {
                rsc.add(StatusCode.NO_RESPONSE);
            }
            else {
                rsc.add(getStatusCodeFromVPM(vpm));
                System.out.println("old token: " + getOldTokenFromVPM(vpm));
                System.out.println("_token: " + _token);
                if (!getOldTokenFromVPM(vpm).equals(_token)) {
                    rsc.add(StatusCode.INVALID_TOKEN);
                }
                else {
                    _token = getTokenFromVPM(vpm);
                }
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/

    /**
     * Retrieves the number latest announcements from the General Board.
     * The request is tampered.
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readGeneralTampered(int number) {
        ProtocolMessage pm = new ProtocolMessage("READGENERAL", _pubKey, encryptToken(_token, _serverPubKey), number);
        VerifiableProtocolMessage vpm = requestServerTampered(pm);
        List<Announcement> announcements = new ArrayList<Announcement>();

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
                announcements = getAnnouncementsFromVPM(vpm);
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/

    /**
     * Retrieves the number latest announcements from the General Board.
     * The server's response was dropped.
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readGeneralDropped(int number) {
        ProtocolMessage pm = new ProtocolMessage("READGENERAL", _pubKey, encryptToken(_token, _serverPubKey), number);
        VerifiableProtocolMessage vpm = requestServerDropped(pm);
        List<Announcement> announcements = new ArrayList<Announcement>();

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
                announcements = getAnnouncementsFromVPM(vpm);
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/

    /**
     * Retrieves the number latest announcements from the General Board.
     * The server's response is null.
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readGeneralNull(int number) {
        ProtocolMessage pm = new ProtocolMessage("READGENERAL", _pubKey, encryptToken(_token, _serverPubKey), number);
        VerifiableProtocolMessage vpm = requestServerNull(pm);
        List<Announcement> announcements = new ArrayList<Announcement>();

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
                announcements = getAnnouncementsFromVPM(vpm);
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/

    /**
     * Sends an invalid readGeneral request to the Server.
     * @param number of announcements to be retrieved
     * @return a pair containing a StatusCode of the operation
     * and the list of announcements received 
     */
    /*public AbstractMap.SimpleEntry<StatusCode, List<Announcement>> readGeneralInvalid(int number) {
        ProtocolMessage pm = new ProtocolMessage("READSGENERAL", _pubKey, encryptToken(_token, _serverPubKey), number);
        VerifiableProtocolMessage vpm = requestServer(pm);
        List<Announcement> announcements = new ArrayList<Announcement>();

        StatusCode rsc = null;
        if (vpm == null) {
            rsc = StatusCode.NO_RESPONSE;
        }
        else {
            rsc = getStatusCodeFromVPM(vpm);
            System.out.println("old token: " + getOldTokenFromVPM(vpm));
            System.out.println("_token: " + _token);
            if (!getOldTokenFromVPM(vpm).equals(_token)) {
                rsc = StatusCode.INVALID_TOKEN;
            }
            else {
                _token = getTokenFromVPM(vpm);
                announcements = getAnnouncementsFromVPM(vpm);
            }
        }

        return new AbstractMap.SimpleEntry<>(rsc, announcements);
    }*/
}