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
    private boolean _running = true;

    public ClientMessageHandler(Server server, Socket socket) throws IOException {
        System.out.println("(INFO) Received new client connection");
        _server = server;
        _socket = socket;
        _communication = new Communication();
        _ois = new ObjectInputStream(_socket.getInputStream());
        _oos = new ObjectOutputStream(_socket.getOutputStream());
    }

    @Override
    public void run() {
        String command = "";

        while(!command.equals("LOGOUT") && _running) {
            try {
                VerifiableProtocolMessage vpm = (VerifiableProtocolMessage) _communication.receiveMessage(_ois);

                if (vpm == null || vpm.getProtocolMessage() == null) continue;

                System.out.println("<== Received [" + vpm.getProtocolMessage().getCommand() + "] from client port: " + _socket.getLocalPort());

                if (_server._serverBroadcasts.containsKey(vpm.getProtocolMessage().getPublicKey())) continue;

                command = vpm.getProtocolMessage().getCommand();

                switch (command) {
                    // Register a Client
                    case "REGISTER":
                        // Thread.sleep(10000);
                        registerUser(vpm);
                        break;
                    // Post to Client's Board
                    case "POST":
                        System.out.println("------------------------POST------------------------");
                        System.out.println("_SERVER: " + _server);
                        System.out.println("this: " + this);
                        _server.post(vpm, this);
                        break;
                    //Writeback phase
                    case "WRITEBACK":
                        System.out.println("------------------------WRITEBACK------------------------");
                        _server.writeBack(vpm, this);
                        break;
                    // Post to General Board
                    case "POSTGENERAL":
                        System.out.println("------------------------POSTGENERAL------------------------");
                        _server.postGeneral(vpm, this);
                        break;
                    // Read from specific user
                    case "READ":
                        System.out.println("------------------------READ------------------------");
                        _server.read(vpm, this);
                        break;
                    // Read from General Board
                    case "READGENERAL":
                        System.out.println("------------------------READGENERAL------------------------");
                        _server.readGeneral(vpm, this);
                        break;
                    // Refresh Client Token
                    case "TOKEN":
                        System.out.println("------------------------TOKEN------------------------");
                        refreshToken(vpm);
                        break;
                    case "LOGOUT":
                        System.out.println("------------------------LOGOUT------------------------");
                        closeCommunication();
                        System.out.println("------------------------LOGOUT------------------------");
                        break;
                    default:
                        invalidCommand(vpm);
                        break;
                }
            }
            catch (IOException | ClassNotFoundException e) {
                System.out.println("(INFO) Client at port: " + _socket.getLocalPort() + " has disconnected.");
                _running = false;
                closeCommunication();
                return;
            }
        }
    }

    public void sendMessage(VerifiableProtocolMessage vpm) {
        try {   
            System.out.println("==> Sending [" + vpm.getProtocolMessage().getCommand() + "] to client port: " + _socket.getLocalPort());
            _communication.sendMessage(vpm, _oos);
        }
        catch (IOException e) {
          System.out.println(e);
          _running = false;
          closeCommunication();
          return;
        }
    }

    public void registerUser(VerifiableProtocolMessage vpm) {
        try {
            VerifiableProtocolMessage svpm = _server.registerUser(vpm, this);
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

    public void invalidCommand(VerifiableProtocolMessage vpm) {
        try {
            VerifiableProtocolMessage svpm = _server.invalidCommand(vpm);
            _communication.sendMessage(svpm, _oos);
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public void refreshToken(VerifiableProtocolMessage vpm) {
        VerifiableProtocolMessage svpm = _server.refreshToken(vpm);
        sendMessage(svpm);
        System.out.println("------------------------END TOKEN------------------------");
    }

}
