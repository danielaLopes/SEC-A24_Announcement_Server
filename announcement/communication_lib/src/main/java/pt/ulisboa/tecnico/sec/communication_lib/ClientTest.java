package pt.ulisboa.tecnico.sec.communication_lib;

import java.io.*;
import java.net.*;
public class ClientTest {
    public static void main(String[] args) throws IOException {
        // need host and port, we want to connect to the ServerSocket at port 7777
        Socket socket = new Socket("localhost", 7777);
        System.out.println("Connected!");

        // get the output stream from the socket.
        OutputStream outputStream = socket.getOutputStream();
        // create an object output stream from the output stream so we can send an object through it
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        // make a bunch of messages to send.
        ProtocolMessage pm = new ProtocolMessage("REGISTER");

        System.out.println("Sending messages to the ServerSocket");
        objectOutputStream.writeObject(pm);

        System.out.println("Closing socket and terminating program.");
        socket.close();
    }
}