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
                
                ProtocolMessage pm = (ProtocolMessage) _communication.receiveMessage(_ois);
                System.out.println("Received [" + pm.getCommand() + "] from: " + _socket);
                System.out.println("Received [" + pm.getPublicKey() + "] from: " + _socket);
                command = pm.getCommand();

                switch (command) {
                    // Register a Client
                    case "REGISTER":
                        registerUser();
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
                System.out.println(e);
            }
        }
    }

    public void registerUser() {
        boolean successful = _server.registerUser(pm.getPublicKey());
        StatusCode sc = StatusCode.OK;
        if (!successful)
            sc = StatusCode.DUPLICATE_USER;
        ProtocolMessage pm = new ProtocolMessage(sc);
        _communication.sendMessage(pm, _oos);
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
