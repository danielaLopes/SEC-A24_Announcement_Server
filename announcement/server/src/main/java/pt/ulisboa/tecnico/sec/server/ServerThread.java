package pt.ulisboa.tecnico.sec.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import pt.ulisboa.tecnico.sec.communication_lib.BestEffortBroadcast;
import pt.ulisboa.tecnico.sec.communication_lib.Communication;
import pt.ulisboa.tecnico.sec.communication_lib.VerifiableProtocolMessage;

public class ServerThread extends Thread {

    private Server _server;
    private int _otherPort;
    private int _selfPort;
    private Socket _socket;
    private ServerSocket _serverSocket;
    private ObjectOutputStream _oos;
    private ObjectInputStream _ois;
    private Communication _communication = new Communication();
    private BestEffortBroadcast beb = new BestEffortBroadcast();

    public ServerThread(Server server, int otherPort, int selfPort, ServerSocket serverSocket) {
        _server = server;
        _otherPort = otherPort;
        _selfPort = selfPort;
        _serverSocket = serverSocket;
    }

    public void bestEffortBroadcast(VerifiableProtocolMessage vpm) throws IOException{
        System.out.println("Sending broadcast message");
        System.out.println(_socket);
        beb.pp2pSend(_oos, vpm);
    }

    public void receiveMessage() throws IOException {
        _oos = new ObjectOutputStream(_socket.getOutputStream());
        _ois = new ObjectInputStream(_socket.getInputStream());
        while(true) {
            try{
                VerifiableProtocolMessage vpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);
                System.out.println("Receiving broadcast message");
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
}