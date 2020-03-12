package pt.ulisboa.tecnico.sec.server;

import java.io.*;
import java.net.Socket;

public class ClientConnectionHandler extends Thread {

    private Server _server;
    private Socket _socket;
    private PrintWriter _outStream;
    private BufferedReader _inStream;

    public ClientConnectionHandler(Server server, Socket socket) {
        _server = server;
        _socket = socket;
    }

    @Override
    public void run() {
        try {
            _outStream = new PrintWriter(_socket.getOutputStream(), true);
            _inStream = new BufferedReader(
                    new InputStreamReader(_socket.getInputStream()));

            String inputLine;
            while ((inputLine = _inStream.readLine()) != null) {
                if ("register".equals(inputLine)) {
                    //_server.registerUser();
                }
                // TODO implement all options for client commands
                _outStream.println(inputLine);
            }

            _inStream.close();
            _outStream.close();
            _socket.close();

        } catch(IOException e) {
            System.out.println("Error in communication streams");
        }
    }


}
