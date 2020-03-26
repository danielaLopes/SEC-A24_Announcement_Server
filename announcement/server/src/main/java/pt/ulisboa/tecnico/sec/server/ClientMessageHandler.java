package pt.ulisboa.tecnico.sec.server;

import java.io.*;
import java.net.*;

import pt.ulisboa.tecnico.sec.communication_lib.*;

public class ClientMessageHandler extends Thread {
    private Server _server;
    private Socket _socket;
    private ObjectInputStream _ois;
    private ObjectOutputStream _oos;
    private Communication _communication;

    public ClientMessageHandler(Server server, Socket socket) throws IOException {
        _server = server;
        _socket = socket;
        _communication = new Communication();
        _ois = new ObjectInputStream(_socket.getInputStream());
        _oos = new ObjectOutputStream(_socket.getOutputStream());
    }

    @Override
    public void run() {
        String command = "";

        while(!command.equals("LOGOUT")) {
            try {
                
                VerifiableProtocolMessage vpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);
                System.out.println("Received [" + vpm.getProtocolMessage().getCommand() + "] from: " + _socket);
                System.out.println("Received [" + vpm.getProtocolMessage().getPublicKey() + "] from: " + _socket);
                System.out.println(command);
                command = vpm.getProtocolMessage().getCommand();

                switch (command) {
                    // Register a Client
                    case "REGISTER":
                        registerUser(vpm);
                        break;
                    // Post to Client's Board
                    case "POST":
                        //post();
                        break;
                    // Post to General Board
                    case "POSTGENERAL":
                        //postGeneral();
                        break;
                    // Read from specific user
                    case "READ":
                        //read();
                        break;
                    // Read from General Board
                    case "READGENERAL":
                        //readGeneral();
                        break;
                    case "LOGOUT":
                        closeCommunication();
                        break;
                    default:
                        break;
                }
            }
            catch (IOException | ClassNotFoundException e) {
                System.out.println("lala");
            }
        }
    }

    public void registerUser(VerifiableProtocolMessage vpm) {
        try {
            VerifiableProtocolMessage svpm = _server.registerUser(vpm);
            _communication.sendMessage(svpm, _oos);
        }
        catch (IOException e) {
          System.out.println(e);
        }
    }

    public void closeCommunication() {
        try {
            _communication.close(_socket);
        }
        catch(IOException e) {
            System.out.println("Error closing socket");
        } 
    }

}
