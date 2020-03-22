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
    public static void main(String[] args) //{
            throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, UnrecoverableKeyException {

        System.out.println("Hello world server");
        Server server = new Server(false, 8000, args[0], args[1]);
        server.start();
        //System.out.println(server.loadPrivateKey());
        System.out.println(args[0]);
        System.out.println(args[1]);
        KeyStore keyStore = KeyStorage.loadKeyStore(args[0], "src/main/resources/crypto/server1_keystore.jks");
        System.out.println("keystore: " + keyStore);

        /*PrivateKey privateKey;
        X509Certificate cert;
        try {
            privateKey = KeyPairUtil.loadPrivateKey("src/main/resources/crypto/server_pkcs8");
        } catch (Exception e) {
            System.out.println("Error: Not possible to load private key from file!");
            e.printStackTrace();
            return;
        }
        try {
            cert = KeyPairUtil.loadCertificate("src/main/resources/crypto/server.crt");
        } catch (Exception e) {
            System.out.println("Error: Not possible to load certificate!");
            e.printStackTrace();
            return;
        }
        KeyStorage.storePrivateKey("ola", keyStore, privateKey, cert);*/
        PrivateKey retrievePrivateKey = KeyStorage.loadPrivateKey(args[1], "ola", keyStore);
        System.out.println("retrieved private key: " + retrievePrivateKey);

        // testing
        /*KeyGenerator keyGen = new KeyGenerator();
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
        }*/
        //server.start();

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