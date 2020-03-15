package pt.ulisboa.tecnico.sec.communication_lib;

import java.net.*;
import java.io.*;

public class Communication {
    public ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket socket = new ServerSocket(port);
		return socket;
    }

    public Socket createClientSocket (String addr, int port) throws IOException {
        Socket socket = new Socket(addr, port);
        return socket;
    }

    public Socket accept(ServerSocket serverSocket) throws IOException{
        return serverSocket.accept();
    }

    public void close(Socket socket) throws IOException {
        socket.close();
    }

    public void sendMessage(String message, Socket socket) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeBytes(message);
    }

    public String receiveMessage(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return in.readLine();
    }
}
