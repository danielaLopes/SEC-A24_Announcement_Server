package pt.ulisboa.tecnico.sec.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bouncycastle.jcajce.provider.asymmetric.GOST.Mappings;

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
    private BestEffortBroadcast _beb = new BestEffortBroadcast();

    public ServerThread(Server server, int nServers, int otherPort, int selfPort, ServerSocket serverSocket) {
        _server = server;
        _nServers = nServers;
        _otherPort = otherPort;
        _selfPort = selfPort;
        _serverSocket = serverSocket;
    }

    public void bestEffortBroadcast(VerifiableServerMessage vsm) {
        //System.out.println("Sending broadcast message: " + vsm.getServerMessage().getCommand() + " " + vsm.getServerMessage().getPublicKey());
        //System.out.println(_socket);
        try{
            _beb.pp2pSend(_oos, vsm);
        }
        catch (IOException e) {
            System.out.println("Could not broadcast message" + e);
        }
    }

    public void writeValue(PublicKey clientPubKey, Announcement a) {
        bestEffortBroadcast(createVerifiableServerMessage(_server.getAtomicRegister1N(clientPubKey).write(a)));
    }

    public void readValue(PublicKey clientPubKey) {
        bestEffortBroadcast(createVerifiableServerMessage(_server.getAtomicRegister1N(clientPubKey).read()));
    }

    public void receiveMessage() throws IOException {
        _oos = new ObjectOutputStream(_socket.getOutputStream());
        _ois = new ObjectInputStream(_socket.getInputStream());
        while(true) {
            try{
                VerifiableServerMessage vsm = (VerifiableServerMessage) _communication.receiveMessage(_ois);
                //System.out.println("server sig: " + vsm.getServerMessage().getPublicKey());
                System.out.println("status code: " + verifySignature(vsm).getDescription() + " command " + vsm.getServerMessage().getCommand());
                if(verifySignature(vsm) == StatusCode.OK) {
                    ServerMessage sm = vsm.getServerMessage();
                    //System.out.println("Received broadcast message: " + sm.getCommand() + "from" + _socket.getPort());
                    //PublicKey clientPubKey = sm.getClientMessage().getProtocolMessage().getPublicKey();
                    PublicKey clientPubKey = sm.getClientPubKey();
                    Thread thread = new Thread(){
                        public void run(){
                            AtomicRegister1N ar1N = _server.getAtomicRegister1N(clientPubKey);
                            switch(sm.getCommand()){
                                case "WRITE":
                                    if (ar1N == null) {
                                        // if the server did not receive a message from the client,
                                        // then it doesn't broadcast to other servers and should not perform
                                        // write local either
                                        //ar1N = new AtomicRegister1N(_server, _nServers, sm.getClientMessage());
                                        ar1N = new AtomicRegister1N(_server, _nServers, clientPubKey);
                                        _server.putAtomicRegister1N(clientPubKey, ar1N);
                                    }
                                    bestEffortBroadcast(createVerifiableServerMessage(ar1N.acknowledge(sm)));
                                    break;
                                case "ACK":
                                    // in case an ack is received after delivered
                                    if (ar1N != null) {
                                        _server.getAtomicRegister1N(clientPubKey).deliver();
                                    }
                                    break;
                                case "READ":
                                    if (ar1N == null) {
                                        // if the server did not receive a message from the client,
                                        // then it doesn't broadcast to other servers and should not perform
                                        // write local either
                                        //ar1N = new AtomicRegister1N(_server, _nServers, sm.getClientMessage());
                                        ar1N = new AtomicRegister1N(_server, _nServers, clientPubKey);
                                        _server.putAtomicRegister1N(clientPubKey, ar1N);
                                    }
                                    bestEffortBroadcast(createVerifiableServerMessage(_server.getAtomicRegister1N(clientPubKey).value(sm)));
                                    break;
                                case "VALUE":
                                    if (ar1N != null) {
                                        ServerMessage response = _server.getAtomicRegister1N(clientPubKey).readReturn(sm);
                                        if (response != null)
                                            bestEffortBroadcast(createVerifiableServerMessage(response));
                                    }
                                    break;
                                default:
                                    break;
                        }
                        }
                    };
                    thread.start();  
                }
                else {
                    System.out.println("Could not verify signature");
                }
            }
            catch (IOException | ClassNotFoundException e) {
                //System.out.println(e);
            }
        }
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

    public VerifiableServerMessage createVerifiableServerMessage(ServerMessage sm) {
        try {
            byte[] spm = ProtocolMessageConverter.objToByteArray(sm);
            byte[] signedsm = SignatureUtil.sign(spm, _server.getPrivateKey());
            return new VerifiableServerMessage(sm, signedsm);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            System.out.println(e);
        }
        return null;
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
}