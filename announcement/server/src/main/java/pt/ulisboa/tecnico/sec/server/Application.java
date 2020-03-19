package pt.ulisboa.tecnico.sec.server;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.List;

public class Application {
    public static void main(String[] args) {

        System.out.println("Hello world server");
        Server server = new Server(false, 8000);
        server.start();
        // testing
        KeyGenerator keyGen = new KeyGenerator();
        KeyPair userKeys = keyGen.generateKeyPair("RSA", 1024);
        PublicKey userPubKey = userKeys.getPublic();
        server.registerUser(userPubKey);
        server.postGeneral(userPubKey, "message1",null);
        List<Announcement> announcements1 = server.readGeneral(1);
        for (Announcement announcement: announcements1) {
            System.out.println(announcement.getMessage());
        }
        server.postGeneral(userPubKey, "message2",null);
        List<Announcement> announcements2 = server.readGeneral(2);
        for (Announcement announcement: announcements2) {
            System.out.println(announcement.getMessage());
        }
        //server.start();
    }
}