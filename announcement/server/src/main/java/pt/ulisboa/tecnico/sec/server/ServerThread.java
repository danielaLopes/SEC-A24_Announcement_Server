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
    private ConcurrentHashMap<PublicKey, AtomicRegister1N> _atomicRegisters1N = new ConcurrentHashMap<>(); // shouldnt this be on the server to be shared by all the threads ???????

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

    public void writeLocalValue(PublicKey clientPubKey, Announcement a) {
        _atomicRegisters1N.get(clientPubKey).writeLocal(a);
    }

    public void writeValue(PublicKey clientPubKey, Announcement a) {
        bestEffortBroadcast(createVerifiableServerMessage(_atomicRegisters1N.get(clientPubKey).write(a)));
    }

    public void readLocalValue(PublicKey clientPubKey) {
        _atomicRegisters1N.get(clientPubKey).readLocal();
    }

    public void readValue(PublicKey clientPubKey) {
        bestEffortBroadcast(createVerifiableServerMessage(_atomicRegisters1N.get(clientPubKey).read()));
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
                    PublicKey clientPubKey = sm.getClientMessage().getProtocolMessage().getPublicKey();
                    Thread thread = new Thread(){
                        public void run(){
                        switch(sm.getCommand()){
                                case "WRITE":
                                    AtomicRegister1N ar1N = _atomicRegisters1N.get(clientPubKey);
                                    if (ar1N == null) {
                                        // if the server did not receive a message from the client,
                                        // then it doesn't broadcast to other serversand should not perform
                                        // write local either
                                        _atomicRegisters1N.put(clientPubKey, new AtomicRegister1N(
                                                _server, _nServers, sm.getClientMessage()));
                                    }
                                    else {
                                        bestEffortBroadcast(createVerifiableServerMessage(ar1N.acknowledge(sm)));
                                    }
                                    break;
                                case "ACK":
                                    _atomicRegisters1N.get(clientPubKey).deliver();
                                    break;
                                case "READ":
                                    bestEffortBroadcast(createVerifiableServerMessage(_atomicRegisters1N.get(clientPubKey).value(sm)));
                                    break;
                                case "VALUE":
                                    ServerMessage response = _atomicRegisters1N.get(clientPubKey).readReturn(sm);
                                    if (response != null) bestEffortBroadcast(createVerifiableServerMessage(response));
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

    public void addAtomicRegisters1N(PublicKey p, AtomicRegister1N a) { _atomicRegisters1N.put(p, a); }

    public Server getServer() {return _server; }
}