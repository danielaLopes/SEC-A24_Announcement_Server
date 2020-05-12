package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.ProtocolMessage;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;

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

    public StatusCode sendDifferentMessages() {
        System.out.println("Send different messages");
        Map<PublicKey, ProtocolMessage> pms = new HashMap<>();
        for (Map.Entry<PublicKey, CommunicationServer> entry : getServerCommunications().entrySet()) {

            // decide a random operation for each server
            int randCmdIndex = new Random().nextInt(_commands.length);
            String randCmd = _commands[randCmdIndex];

            ProtocolMessage pm;

            System.out.println("Before creating messages");

            if (randCmd.equals("POST")) {
                pm = createRandomPost("POST", entry.getValue());
            }
            else if (randCmd.equals("READ")) {
                pm = createRandomRead("READ", entry.getValue());
            }
            else if (randCmd.equals("POSTGENERAL")) {
                pm = createRandomPost("POSTGENERAL", entry.getValue());
            }
            else {
                pm = createRandomRead("READGENERAL", entry.getValue());
            }
            //p.setAtomicRegisterMessages(arm.getBytes());
            pms.put(entry.getKey(), pm);
        }
        return broadcastToServers(pms);
    }

    public ProtocolMessage createRandomPost(String cmd, CommunicationServer serverCommunication) {

        int randMsgIndex = new Random().nextInt(_messages.length);
        String randMsg = _messages[randMsgIndex];

        Announcement a = new Announcement(randMsg, new ArrayList<>());

        return new ProtocolMessage("POST", _pubKey, a, serverCommunication.getToken());
    }

    public ProtocolMessage createRandomRead(String cmd, CommunicationServer serverCommunication) {

        int randNumber = new Random().nextInt(10);

        if (cmd.equals("READ"))
            return new ProtocolMessage("READ", _pubKey, serverCommunication.getToken(), randNumber, _pubKey);
        else
            return new ProtocolMessage("READGENERAL", _pubKey, serverCommunication.getToken(), randNumber);
    }

    public StatusCode broadcastToServers(Map<PublicKey, ProtocolMessage> pms) {

        for (Map.Entry<PublicKey, ProtocolMessage> pm : pms.entrySet()) {
            Thread thread = new Thread() {
                public void run() {
                    VerifiableProtocolMessage response = requestServer(pm.getValue(), getServerCommunications().get(pm.getKey()));
                    getServerResponses().put(pm.getKey(), response);

                    System.out.println("Added new message, we now have: " + getServerResponses().size());

                    printResponse(response.getProtocolMessage());
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

    public void colludeWithServer(int port) {

    }
}
