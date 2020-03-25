package pt.ulisboa.tecnico.sec.server;

import java.io.*;

public class ClientMessageHandler extends Thread {
    private Server _server;
    private String _command;

    public ClientMessageHandler(Server server, String command) {
        _server = server;
        _command = command;
    }

    @Override
    public void run() {
        int message = 1;

        switch (message) {
            // Post to Client's Board
            case 1:
                //post();
                break;
            // Post to General Board
            case 2:
                //postGeneral();
                break;
            // Read from specific user
            case 3:
                //read();
                break;
            // Read from General Board
            case 4:
                //readGeneral();
                break;
            default:
                break;
        }
    }

}
