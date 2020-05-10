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

    private ConcurrentHashMap<PublicKey, ServerMessage> _serverMessageQueue = new ConcurrentHashMap<>();

    public ServerThread(Server server, int nServers, int otherPort, int selfPort, ServerSocket serverSocket) {
        _server = server;
        _nServers = nServers;
        _otherPort = otherPort;
        _selfPort = selfPort;
        _serverSocket = serverSocket;
    }

    public void sendServerMessage(ServerMessage sm) {
        //System.out.println("Sending broadcast message: " + vsm.getServerMessage().getCommand() + " " + vsm.getServerMessage().getPublicKey());
        //System.out.println(_socket);
        VerifiableServerMessage vsm = _server.createVerifiableServerMessage(sm);
        try{
            _communication.sendMessage(vsm, _oos);
        }
        catch (IOException e) {
            System.out.println("Could not broadcast message" + e);
        }
    }

    public void receiveMessage() throws IOException {
        _oos = new ObjectOutputStream(_socket.getOutputStream());
        _ois = new ObjectInputStream(_socket.getInputStream());
        // TODO: put timeout
        while(true) {
            try {
                VerifiableServerMessage vsm = (VerifiableServerMessage) _communication.receiveMessage(_ois);

                Thread thread = new Thread() {
                    public void run() {
                        //System.out.println("server sig: " + vsm.getServerMessage().getPublicKey());
                        System.out.println("status code: " + verifySignature(vsm).getDescription() + " command " + vsm.getServerMessage().getCommand());
                        if (verifySignature(vsm) == StatusCode.OK) {
                            ServerMessage sm = vsm.getServerMessage();
                            System.out.println("Received broadcast message: " + sm.getCommand() + "from" + _socket.getPort());
                            switch (sm.getCommand()) {
                                case "SERVER_POST":
                                    handleServerPost(sm);
                                    break;
                                case "ECHO":
                                    handleEcho(sm);
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            System.out.println("Could not verify signature");
                        }
                    }
                };
                thread.start();
            } catch (IOException | ClassNotFoundException e) {
                //System.out.println(e);
            }
        }
    }

    public void handleServerPost(ServerMessage sm) {
        System.out.println("handleServerPost");
        VerifiableProtocolMessage vpm = sm.getClientMessage();
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        if (_server.verifySignature(vpm).equals(StatusCode.OK)) {
            if (_server._serverBroadcasts.containsKey(clientPubKey)) {
                ServerBroadcast sb = _server._serverBroadcasts.get(clientPubKey);
                sendServerMessage(sb.echo(sm));
            }
            else {
                addServerMessageToQueue(clientPubKey, sm);
            }
        }
    }

    public void sendQueueMessages(PublicKey clientPubKey) {
        System.out.println("sendQueueMessages");
        ServerBroadcast sb = _server._serverBroadcasts.get(clientPubKey);
        for (Map.Entry<PublicKey, ServerMessage> entry : _serverMessageQueue.entrySet()) {
            System.out.println("sending queue message");
            sendServerMessage(sb.echo(entry.getValue()));
        }
    }

    public void handleEcho(ServerMessage sm) {
        System.out.println("handleEcho");
        VerifiableProtocolMessage vpm = sm.getClientMessage();
        PublicKey clientPubKey = vpm.getProtocolMessage().getPublicKey();
        ServerBroadcast sb = _server._serverBroadcasts.get(clientPubKey);
        if (sb == null) return;
        sendServerMessage(sb.ready(sm));
    }

    public void acceptCommunications() {
        try {
            _socket = _communication.accept(_serverSocket);
            
            System.out.println("Server has accepted communications in port:" + _selfPort);

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



            receiveMessage();
        }
        catch(IOException e) {
            System.out.println("Error starting client socket to communicate with server port " + _otherPort);
            acceptCommunications();
        }
    }

    public StatusCode verifySignature(VerifiableServerMessage vsm) {
        try {
            byte[] bsm = ProtocolMessageConverter.objToByteArray(vsm.getServerMessage());
            boolean verified = SignatureUtil.verifySignature(vsm.getSignedServerMessage(),
                    vsm.getServerMessage().getPublicKey(), bsm);
            if (verified == true)
                return StatusCode.OK;
            else
                return StatusCode.INVALID_SIGNATURE;
        } catch (NoSuchAlgorithmException e) {
            System.out.println(StatusCode.INVALID_ALGORITHM + "\n" + e);
            return StatusCode.INVALID_ALGORITHM;
        } catch (InvalidKeyException e) {
            System.out.println(StatusCode.INVALID_KEY + "\n" + e);
            return StatusCode.INVALID_KEY;
        } catch (SignatureException e) {
            System.out.println(StatusCode.INVALID_SIGNATURE + "\n" + e);
            return StatusCode.INVALID_SIGNATURE;
        }
    }

    public Server getServer() {return _server; }

    public void addServerMessageToQueue(PublicKey clientPubKey, ServerMessage sm) {
        _serverMessageQueue.put(clientPubKey, sm);
    }
}