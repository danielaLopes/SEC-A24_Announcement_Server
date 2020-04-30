package pt.ulisboa.tecnico.sec.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;

import pt.ulisboa.tecnico.sec.communication_lib.*;
import pt.ulisboa.tecnico.sec.crypto_lib.*;

public class ServerThread extends Thread {

    private Server _server;
    private int _otherPort;
    private int _selfPort;
    private Socket _socket;
    private ServerSocket _serverSocket;
    private ObjectOutputStream _oos;
    private ObjectInputStream _ois;
    private Communication _communication = new Communication();
    private BestEffortBroadcast _beb = new BestEffortBroadcast();
    private AtomicRegister1N _atomicRegister1N;

    public ServerThread(Server server, int otherPort, int selfPort, ServerSocket serverSocket, AtomicRegister1N atomicRegister1N) {
        _server = server;
        _otherPort = otherPort;
        _selfPort = selfPort;
        _serverSocket = serverSocket;
        _atomicRegister1N = atomicRegister1N;
    }

    public void bestEffortBroadcast(VerifiableServerMessage vsm) {
        System.out.println("Sending broadcast message");
        System.out.println(_socket);
        try{
            _beb.pp2pSend(_oos, vsm);
        }
        catch (IOException e) {
            System.out.println("Could not broadcast message" + e);
        }
    }

    public void writeLocalValue(Announcement a) {
        _atomicRegister1N.writeLocal(a);
    }

    public void writeValue(Announcement a) {
        bestEffortBroadcast(createVerifiableServerMessage(_atomicRegister1N.write(a, _server.getPublicKey())));
    }

    public void receiveMessage() throws IOException {
        _oos = new ObjectOutputStream(_socket.getOutputStream());
        _ois = new ObjectInputStream(_socket.getInputStream());
        while(true) {
            try{
                VerifiableServerMessage vsm = (VerifiableServerMessage) _communication.receiveMessage(_ois);
                if(verifySignature(vsm) == StatusCode.OK) {
                    ServerMessage sm = vsm.getServerMessage();
                    System.out.println("Received broadcast message");
                    Thread thread = new Thread(){
                        public void run(){
                        switch(sm.getCommand()){
                                case "WRITE":
                                    bestEffortBroadcast(createVerifiableServerMessage(_atomicRegister1N.acknowledge(sm)));
                                    break;
                                case "ACK":
                                    _atomicRegister1N.writeReturn(sm);
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