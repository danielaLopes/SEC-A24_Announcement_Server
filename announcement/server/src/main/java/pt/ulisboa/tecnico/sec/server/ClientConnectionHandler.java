package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.communication_lib.Communication;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

public class ClientConnectionHandler extends Thread {

    private Server _server;
    private ServerSocket _serverSocket;
    private Socket _socket;
    private PrintWriter _outStream;
    private BufferedReader _inStream;
    private Communication _communication;

    public ClientConnectionHandler(Server server) {
        _server = server;
        _communication = new Communication();
    }

    @Override
    public void run() {
        try {
            startClientCommunication();

            String message = _communication.receiveMessage(_socket);
            System.out.println(message);

        } catch(IOException e) {
            System.out.println("Error in communication streams");
        }
    }

    public void startClientCommunication() {
        try {
            _serverSocket = _communication.createServerSocket(8000);
            _socket = _communication.accept(_serverSocket);
        }
        catch(IOException e) {
            System.out.println("Error starting server socket");
        }
    }


}
