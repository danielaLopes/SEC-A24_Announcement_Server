package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.*;
import java.net.*;
public class ServerTest {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ServerSocket ss = new ServerSocket(7777);
        System.out.println("ServerSocket awaiting connections...");
        Socket socket = ss.accept(); // blocking call, this will wait until a connection is attempted on this port.
        System.out.println("Connection from " + socket + "!");

        // get the input stream from the connected socket
        InputStream inputStream = socket.getInputStream();
        // create a DataInputStream so we can read data from it.
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

        // read the list of messages from the socket
        ProtocolMessage listOfMessages = (ProtocolMessage) objectInputStream.readObject();
        System.out.println("Received [" + listOfMessages.getCommand() + "] messages from: " + socket);
        
        System.out.println("Closing sockets.");
        ss.close();
        socket.close();
    }
}
