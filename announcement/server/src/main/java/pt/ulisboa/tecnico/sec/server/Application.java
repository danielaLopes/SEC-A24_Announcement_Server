package pt.ulisboa.tecnico.sec.server;

import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

public class Application {
    public static void main(String[] args) throws NoSuchAlgorithmException {

        if(args.length != 3) {
            System.out.println("Args: <keyStorePassword> <entryPassword> <alias>");
            return;
        }

        System.out.println("Hello world server");
        Server server = new Server(false, 8000, args[0].toCharArray(), args[1].toCharArray(), args[2]);
        server.start();

        // testing
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey userPubKey = kp.getPublic();

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

        /*catch (FileNotFoundException e) {
            System.out.println("Error: File " + file.getAbsolutePath() + " was not found!");
        } catch (IOException e) {
            System.out.println("Error: Not possible to close FileInputStream");
        } catch (CertificateException e) {
            System.out.println("Error: Not possible to load Certificate");
            e.printStackTrace();
        }*/
    }
}