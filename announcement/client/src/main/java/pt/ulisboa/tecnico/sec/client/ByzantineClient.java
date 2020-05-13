package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.client.Client;
import pt.ulisboa.tecnico.sec.client.ClientUI;
import pt.ulisboa.tecnico.sec.client.CommunicationServer;
import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.UUIDGenerator;

import java.io.IOException;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ByzantineClient extends Client {

    private String[] _commands = new String[]{"POST", "READ", "POSTGENERAL", "READGENERAL"};
    private String[] _messages = new String[]{"a1", "a2", "a3", "a4"};

    public ByzantineClient(String pubKeyPath, String keyStorePath,
                           String keyStorePasswd, String entryPasswd, String alias,
                           int nServers, int nFaults, List<String> otherUsersPubKeyPaths, ClientUI clientUI) {
        super(pubKeyPath, keyStorePath, keyStorePasswd, entryPasswd, alias,
                nServers, nFaults, otherUsersPubKeyPaths, clientUI);
    }

    public ByzantineClient(String pubKeyPath, String keyStorePath,
                           String keyStorePasswd, String entryPasswd, String alias,
                           int nServers, int nFaults) {
        super(pubKeyPath, keyStorePath, keyStorePasswd, entryPasswd, alias,
                nServers, nFaults);
    }

    public StatusCode sendDifferentMessages(String code) {
        //System.out.println("Send different messages");
        ConcurrentMap<PublicKey, ProtocolMessage> pms = new ConcurrentHashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : getServerCommunications().entrySet()) {

            // decide a random operation for each server
            int randCmdIndex = new Random().nextInt(_commands.length);
            String randCmd = _commands[randCmdIndex];

            ProtocolMessage pm;

            if (randCmd.equals("POST")) {
                pm = createRandomPost("POST", entry.getValue(), code);
            }
            else if (randCmd.equals("READ")) {
                pm = createRandomRead("READ", entry.getValue(), code);
            }
            else if (randCmd.equals("POSTGENERAL")) {
                pm = createRandomPost("POSTGENERAL", entry.getValue(), code);
            }
            else {
                pm = createRandomRead("READGENERAL", entry.getValue(), code);
            }

            pms.put(entry.getKey(), pm);
        }
        return broadcastToServers(pms);
    }

    public ProtocolMessage createRandomPost(String cmd, CommunicationServer serverCommunication, String code) {

        int randMsgIndex = new Random().nextInt(_messages.length);
        String randMsg = _messages[randMsgIndex];

        Announcement a = new Announcement(randMsg, new ArrayList<>());
        String announcementID = UUIDGenerator.generateUUID();
        a.setAnnouncementID(announcementID);
        a.setPublicKey(_pubKey);

        ProtocolMessage pm;

        if (cmd.equals("POST"))
            pm = new ProtocolMessage("POST", _pubKey, a, serverCommunication.getToken());
        else
            pm = new ProtocolMessage("POSTGENERAL", _pubKey, a, serverCommunication.getToken());

        if (code.equals("OK")) {
            int rid;
            int wts;

            if (cmd.equals("POST")) {
                getAtomicRegister1N().write();
                rid = getAtomicRegister1N().getRid();
                wts = getAtomicRegister1N().getWts();
            }
            else {
                getRegularRegisterNN().write();
                rid = getRegularRegisterNN().getRid();
                wts = getRegularRegisterNN().getWts();
            }

            VerifiableAnnouncement va = createVerifiableAnnouncement(a);

            List<VerifiableAnnouncement> values = new ArrayList<>(Arrays.asList(va));

            RegisterMessage arm = new RegisterMessage(rid, wts, values);
            pm.setAtomicRegisterMessages(arm.getBytes());
        }

        return pm;
    }

    public ProtocolMessage createRandomRead(String cmd, CommunicationServer serverCommunication, String code) {

        int randNumber = new Random().nextInt(10);

        ProtocolMessage pm;

        if (cmd.equals("READ"))
            pm = new ProtocolMessage("READ", _pubKey, serverCommunication.getToken(), randNumber, _pubKey);
        else
            pm = new ProtocolMessage("READGENERAL", _pubKey, serverCommunication.getToken(), randNumber);

        RegisterMessage arm;
        if (code.equals("OK")) {
            if (cmd.equals("POST")) {
                arm = getAtomicRegister1N().read();
            } else {
                arm = getRegularRegisterNN().read();
            }
            pm.setAtomicRegisterMessages(arm.getBytes());
        }
        return pm;
    }

    public StatusCode broadcastToServers(ConcurrentMap<PublicKey, ProtocolMessage> pms) {

        for (Map.Entry<PublicKey, ProtocolMessage> pm : pms.entrySet()) {
            Thread thread = new Thread() {
                public void run() {

                    if (pm.getValue().getCommand().equals("POST") || pm.getValue().getCommand().equals("POSTGENERAL"))
                        System.out.println("Sent message: " + pm.getValue().getCommand() + " " +
                                pm.getValue().getPostAnnouncement().getAnnouncement());
                    else System.out.println("Sent message: " + pm.getValue().getCommand());
                    System.out.flush();

                    VerifiableProtocolMessage response = requestServer(pm.getValue(), getServerCommunications().get(pm.getKey()));

                    if (response == null) System.out.println("Did not receive a response");
                    else {
                        getServerResponses().put(pm.getKey(), response);
    
                        System.out.println("Added new message, we now have: " + getServerResponses().size());
    
                        printResponse(response.getProtocolMessage());
                        System.out.flush();
                    }
                }

            };
            thread.start();
        }

        System.out.println("After sending messages");

        while(getServerResponses().size() < _nServers) {
            // do nothing
            //Thread.sleep(1000);
            //System.out.println("getServerResponses().size() " + getServerResponses().size());
        }
        System.out.println("After receiving responses");
        StatusCode finalSc = verifyStatusConsensus();
        resetResponses();

        return finalSc;
    }

    public void printResponse(ProtocolMessage response) {
        String cmd = response.getCommand();

        System.out.println("------------ RESPONSE ------------");
        System.out.println("Response status: " + response.getStatusCode());
        System.out.println("Response vpm: " + response.getStatusCode());

        if (cmd.equals("POST") || cmd.equals("POSTGENERAL")) {

        }
        else {

        }
        System.out.println("-----------------------------------");
    }

    /**
     * Client sends post messages only to a colluding server to increment
     * that servers timestamp with correctly signed posts.
     * @param port
     */
    public VerifiableProtocolMessage colludeWithServer(int port) {
        System.out.println("Collude with server in port: " + port);
        CommunicationServer colludingServer = null;
        for (CommunicationServer comm : getServerCommunications().values()) {
            System.out.println("Checking server with port: " + comm.getPort());
            if (port == comm.getPort()) {
                colludingServer = comm;
                break;
            }
        }

        System.out.println("colludingServer: " + colludingServer);

        Announcement a = new Announcement(_messages[0], new ArrayList<>());
        a.setPublicKey(_pubKey);

        getAtomicRegister1N().write();
        int rid = getAtomicRegister1N().getRid();
        int wts = getAtomicRegister1N().getWts();

        VerifiableAnnouncement va = createVerifiableAnnouncement(a);
        List<VerifiableAnnouncement> values = new ArrayList<VerifiableAnnouncement>(Arrays.asList(va));

        RegisterMessage arm = new RegisterMessage(rid, wts, values);

        ProtocolMessage p = new ProtocolMessage("POSTGENERAL", _pubKey, a, colludingServer.getToken());
        p.setAtomicRegisterMessages(arm.getBytes());

        VerifiableProtocolMessage vpm = createVerifiableMessage(p);

        VerifiableProtocolMessage response = sendMessageToServer(vpm, colludingServer);
        System.out.println("Colluding server response: " + response);

        return response;
    }

    public VerifiableProtocolMessage sendMessageToServer(VerifiableProtocolMessage vpm, CommunicationServer serverCommunication) {

        VerifiableProtocolMessage response = null;
        try {
            _communication.sendMessage(vpm, serverCommunication.getObjOutStream());
            response = (VerifiableProtocolMessage) _communication.receiveMessage(serverCommunication.getObjInStream());
        }
        catch (IOException e) {
            System.out.println("Could not send message to server");
        }
        catch (ClassNotFoundException e) {
            System.out.println("Class not found!");
        }

        return response;
    }
}
