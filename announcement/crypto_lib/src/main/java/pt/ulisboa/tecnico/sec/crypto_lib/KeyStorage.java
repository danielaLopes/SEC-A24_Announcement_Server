package pt.ulisboa.tecnico.sec.crypto_lib;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class KeyStorage {

    private static char[] retrievePassword(String pathToPasswd) throws FileNotFoundException {
        // retrieve password from file
        File file = new File(pathToPasswd);
        Scanner scanner = new Scanner(file);
        return scanner.nextLine().toCharArray();
    }

    public static KeyStore createKeyStore(String pathToPasswd, String filename)
            throws KeyStoreException, NoSuchAlgorithmException,
            IOException, CertificateException{

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        char[] password = retrievePassword(pathToPasswd);
        System.out.println("password: " + password);
        keyStore.load(null, password);

        FileOutputStream fos = new FileOutputStream(filename);
        keyStore.store(fos, password);
        fos.close();

        return keyStore;
    }

    public static KeyStore loadKeyStore(String pathToPasswd, String filename) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        keyStore.load(new FileInputStream(filename), retrievePassword(pathToPasswd));

        return keyStore;
    }

    /**
     * NOT WORKING
     * @param pathToPasswd
     * @param alias
     * @param keyStore
     * @param privateKey
     * @param cert
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public static void storePrivateKey(String pathToPasswd, String alias, KeyStore keyStore, PrivateKey privateKey, X509Certificate cert)
            throws KeyStoreException, FileNotFoundException {

        X509Certificate[] certificateChain = new X509Certificate[1];
        certificateChain[0] = cert;

        keyStore.setKeyEntry(alias, privateKey, retrievePassword(pathToPasswd), certificateChain);
    }

    public static PrivateKey loadPrivateKey(String pathToPasswd, String alias, KeyStore keyStore)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException, FileNotFoundException {

        return (PrivateKey) keyStore.getKey(alias, retrievePassword(pathToPasswd));
    }
}
