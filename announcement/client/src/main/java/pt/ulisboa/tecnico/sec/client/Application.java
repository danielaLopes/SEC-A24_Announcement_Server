package pt.ulisboa.tecnico.sec.client;

import java.util.List;
import java.util.ArrayList;

public class Application {
    public static void main(String[] args) {
        System.out.println("Hello world client");

        List<String> otherUsersPubKeys = new ArrayList<String>();
        for (int i = 1; i <= Integer.parseInt(args[5]); i++) {
            otherUsersPubKeys.add(args[5+i]);
        }

        ClientUI clientUi = new ClientUI(args[0], args[1], args[2], args[3], args[4], otherUsersPubKeys);
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