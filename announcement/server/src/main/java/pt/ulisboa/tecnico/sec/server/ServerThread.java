package pt.ulisboa.tecnico.sec.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.*;

public class ServerThread extends Thread {

    private Server _server;
    private int _nServers;
    private int _otherPort;
    private int _selfPort;
    private Socket _socket;
    private ServerSocket _serverSocket;
    private ObjectOutputStream _oos;
    private ObjectInputStream _ois;
    private Communication _communication = new Communication();

    protected ConcurrentHashMap<PublicKey, ServerMessage> _serverMessageQueue = new ConcurrentHashMap<>();

    public ServerThread(Server server, int nServers, int otherPort, int selfPort, ServerSocket serverSocket) {
        _server = server;
        _nServers = nServers;
        _otherPort = otherPort;
        _selfPort = selfPort;
        _serverSocket = serverSocket;
    }

    public void sendServerMessage(ServerMessage sm) {
        // TODO: sometimes sm is null
        System.out.println("Sending broadcast message: " + sm.getCommand());
        System.out.flush();
        VerifiableServerMessage vsm = _server.createVerifiableServerMessage(sm);
        try{
            _communication.sendMessage(vsm, _oos);
        }
        catch (IOException e) {
            System.out.println("Server in port " + _otherPort + " is down");
        }
    }

    public void receiveMessage() throws IOException {
        // TODO: put timeout
        while(true) {
            try {
                VerifiableServerMessage vsm = (VerifiableServerMessage) _communication.receiveMessage(_ois);

                Thread thread = new Thread() {
                    public void run() {
                        //System.out.println("server sig: " + vsm.getServerMessage().getPublicKey());
                        if (_server.verifyServerSignature(vsm).equals(StatusCode.OK)) {
                            StatusCode sc = verifyServerMessage(vsm);
                            if (sc.equals(StatusCode.OK)) {
                                ServerMessage sm = vsm.getServerMessage();
                                System.out.println("Received broadcast message: " + sm.getCommand() + "from" + _socket.getPort());
                                System.out.flush();
                                switch (sm.getCommand()) {
                                    // broadcaster server initiates broadcast
                                    case "SERVER_POST":
                                        handleServerBroadcast(sm);
                                        break;
                                    case "SERVER_READ":
                                        handleServerBroadcast(sm);
                                        break;
                                    case "SERVER_POSTGENERAL":
                                        handleServerBroadcast(sm);
                                        break;
                                    case "SERVER_READGENERAL":
                                        handleServerBroadcast(sm);
                                        break;
                                    // other servers answer our broadcast
                                    case "ECHO":
                                        handleEcho(vsm);
                                        break;
                                    // broadcaster server sends ready message
                                    case "FINAL":
                                        handleFinal(vsm);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            else {
                                System.out.println("Server message is invalid: " + sc);
                            }
                        } else {
                            System.out.println("Could not verify signature");
                        }
                    }
                };
                thread.start();
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                System.out.println("Server in port " + _otherPort + " is down");
                acceptCommunications();
            }
        }
    }

    public void handleServerBroadcast(ServerMessage sm) {
        // System.out.println("handleServerBroadcast");

        VerifiableProtocolMessage vpm = sm.getClientMessage();
        verifyClientMessage(vpm);
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        if (_server.verifySignature(vpm).equals(StatusCode.OK)) {
            if (_server._serverBroadcasts.containsKey(clientPubKey)) {
                ServerBroadcast sb = _server._serverBroadcasts.get(clientPubKey);
                sb.localEcho();
                ServerMessage smToEcho = sb.echo(sm);
                if (smToEcho != null)
                    sendServerMessage(smToEcho);
            }
            else {
                addServerMessageToQueue(clientPubKey, sm);
            }
        }
    }

    public void sendQueueMessages(PublicKey clientPubKey) {
        System.out.println("sendQueueMessages");
        ServerBroadcast sb = _server._serverBroadcasts.get(clientPubKey);
        if(_serverMessageQueue.containsKey(clientPubKey)) {
            sb.localEcho();
            sendServerMessage(sb.echo(_serverMessageQueue.get(clientPubKey)));
            _serverMessageQueue.remove(clientPubKey);
        }
    }

    public void handleEcho(VerifiableServerMessage vsm) {
        // System.out.println("handleEcho");
        ServerMessage sm = vsm.getServerMessage();
        VerifiableProtocolMessage vpm = sm.getClientMessage();
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        ServerBroadcast sb = _server._serverBroadcasts.get(clientPubKey);
        if (sb == null) return;
        ServerMessage s = sb.ready(vsm);
        if(s != null) {
            _server.sendToAllServers(s);
            //sb.localReady();
        }
    }

    public void handleFinal(VerifiableServerMessage vsm) {
        // System.out.println("handleFinal");
        ServerMessage sm = vsm.getServerMessage();
        VerifiableProtocolMessage vpm = sm.getClientMessage();
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        ServerBroadcast sb = _server._serverBroadcasts.get(clientPubKey);
        if (sb == null) return;
        sb.finalDelivery(vsm);
    }

    public void acceptCommunications() {
        try {
            _socket = _communication.accept(_serverSocket);
            
            System.out.println("Server has accepted communications in port:" + _selfPort);
            
            _oos = new ObjectOutputStream(_socket.getOutputStream());
            _ois = new ObjectInputStream(_socket.getInputStream());
            receiveMessage();

            

        } catch (IOException e) {
            System.out.println("Could not be a server of other servers.");
        }
    }

    @Override
    public void run() {
        try {
            _socket = new Socket("localhost", _otherPort);

            System.out.println("Starting client socket in port: " + _otherPort);
            
            _oos = new ObjectOutputStream(_socket.getOutputStream());
            _ois = new ObjectInputStream(_socket.getInputStream());
            receiveMessage();
        }
        catch(IOException e) {
            System.out.println("Error starting client socket to communicate with server port " + _otherPort);
            acceptCommunications();
        }
    }

    public StatusCode verifyServerMessage(VerifiableServerMessage vsm) {
        ServerMessage sm = vsm.getServerMessage();
        System.out.println("--- verifying server message ---: " + sm.getCommand());
        if (sm.getCommand() == null || sm.getPublicKey() == null || sm.getClientMessage() == null) {
            return StatusCode.NULL_FIELD;
        }
        return StatusCode.OK;
    }

    // TODO: finish
    public StatusCode verifyClientMessage(VerifiableProtocolMessage vpm) {
        ProtocolMessage pm = vpm.getProtocolMessage();
        // TODO: verify client signature ?????
        if (pm.getCommand() == null) {
            return StatusCode.NULL_FIELD;
        }
        if (pm.getCommand().equals("POST")) {
            //_server.verifyPost();
        }
        else if (pm.getCommand().equals("READ")) {

        }
        else if (pm.getCommand().equals("POSTGENERAL")) {

        }
        else if (pm.getCommand().equals("READGENERAL")) {

        }
        else {
            return StatusCode.INVALID_COMMAND;
        }
        return StatusCode.OK;
    }

    public Server getServer() {return _server; }

    public void addServerMessageToQueue(PublicKey clientPubKey, ServerMessage sm) {
        _serverMessageQueue.put(clientPubKey, sm);
    }
}