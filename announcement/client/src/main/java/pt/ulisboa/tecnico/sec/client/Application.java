package pt.ulisboa.tecnico.sec.client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Application {
    public static void main(String[] args) {
        System.out.println("Hello world client");

        ClientUI clientUi = new ClientUI();
        clientUi.start();
        /*
        try {
            System.out.println("before opening socket");
            Socket socket = new Socket("localhost", 8000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader((new InputStreamReader(socket.getInputStream())));

            System.out.println("before sending message");

            out.println("Hi server!");
            String res = in.readLine();
            System.out.println("Server answered: " + res);

            System.out.println("after sending message");

            in.close();
            out.close();
            socket.close();
        } catch(IOException e) {
            System.out.println("Error initiating socket");
        }
        */



    }
}