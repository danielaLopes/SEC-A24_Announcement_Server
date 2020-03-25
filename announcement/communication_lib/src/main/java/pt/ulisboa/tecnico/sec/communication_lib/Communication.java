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

    /*    public void sendMessage(String message, Socket socket) throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        out.writeBytes(message);
    }*/

    public void sendMessage(Object pm, Socket socket) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        out.writeObject(pm);
    }

    public void sendMessage(Object pm, ObjectOutputStream out) throws IOException {
        out.writeObject(pm);
    }

    public Object receiveMessage(Socket socket) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
        return in.readObject();
    }

    public Object receiveMessage(ObjectInputStream in) throws IOException, ClassNotFoundException {
        return in.readObject();
    }
}
