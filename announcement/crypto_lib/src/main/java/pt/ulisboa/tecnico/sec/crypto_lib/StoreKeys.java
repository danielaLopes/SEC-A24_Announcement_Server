package pt.ulisboa.tecnico.sec.crypto_lib;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * NOT WORKING
 */
public class StoreKeys {
    public static void main(String args[]) {
        if(args.length != 6) {
            System.out.println("This program stores a private key in a keyStore");
            System.out.println("Usage: StoreKeys <keyStorePassword> <entryPassword> <alias> <keyStore.jks> <privateKey.key> <certificate.crt>");
            return;
        }

        KeyStore keyStore;
        PrivateKey privateKey;
        X509Certificate cert;
        try {
            keyStore = KeyStorage.loadKeyStore(args[0].toCharArray(), args[3]);
        } catch (Exception e) {
            System.out.println("Error: Not possible to load keystore!");
            e.printStackTrace();
            return;
        }
        try {
            privateKey = KeyPairUtil.loadPrivateKey(args[4]);
        } catch (Exception e) {
            System.out.println("Error: Not possible to load private key from file!");
            e.printStackTrace();
            return;
        }
        try {
            cert = KeyPairUtil.loadCertificate(args[5]);
        } catch (Exception e) {
            System.out.println("Error: Not possible to load certificate!");
            e.printStackTrace();
            return;
        }

        try {
            KeyStorage.storePrivateKey(args[1].toCharArray(), args[2], keyStore, privateKey, cert);
        } catch (Exception e) {
            System.out.println("Error: Not possible to store private key in keystore!");
            e.printStackTrace();
            return;
        }
    }
}
