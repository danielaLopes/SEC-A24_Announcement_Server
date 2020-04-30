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
        System.out.println("received new client connection");
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
                //System.out.println("Received [" + vpm.getProtocolMessage().getPublicKey() + "] from: " + _socket);
                command = vpm.getProtocolMessage().getCommand();

                switch (command) {
                    // Register a Client
                    case "REGISTER":
                        // Thread.sleep(10000);
                        registerUser(vpm);
                        break;
                    // Post to Client's Board
                    case "POST":
                        _server.post(vpm, this);
                        break;
                    // Post to General Board
                    case "POSTGENERAL":
                        // Thread.sleep(10000);
                        postGeneral(vpm);
                        break;
                    // Read from specific user
                    case "READ":
                        read(vpm);
                        break;
                    // Read from General Board
                    case "READGENERAL":
                        // Thread.sleep(10000);
                        readGeneral(vpm);
                        break;
                    // Refresh Client Token
                    case "TOKEN":
                        // Thread.sleep(10000);
                        refreshToken(vpm);
                        break;
                    case "LOGOUT":
                        closeCommunication();
                        break;
                    default:
                        invalidCommand(vpm);
                        break;
                }
            }
            // catch (InterruptedException e) {
            // }
            catch (IOException | ClassNotFoundException e) {
                // System.out.println(e);
            }
        }
    }

    public void sendMessage(VerifiableProtocolMessage vpm) {
        try {
            _communication.sendMessage(vpm, _oos);
        }
        catch (IOException e) {
          System.out.println(e);
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

    public void postGeneral(VerifiableProtocolMessage vpm) {
        try {
            VerifiableProtocolMessage svpm = _server.postGeneral(vpm);
            _communication.sendMessage(svpm, _oos);
        }
        catch (IOException e) {
          System.out.println(e);
        }
    }

    public void readGeneral(VerifiableProtocolMessage vpm) {
        try {
            VerifiableProtocolMessage svpm = _server.readGeneral(vpm);
            _communication.sendMessage(svpm, _oos);
        }
        catch (IOException e) {
          System.out.println(e);
        }
    }

    public void read(VerifiableProtocolMessage vpm) {
        try {
            VerifiableProtocolMessage svpm = _server.read(vpm);
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
        try {
            VerifiableProtocolMessage svpm = _server.refreshToken(vpm);
            _communication.sendMessage(svpm, _oos);
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

}
