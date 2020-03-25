package pt.ulisboa.tecnico.sec.crypto_lib;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class KeyStorage {

    public static KeyStore createKeyStore(char[] password, String filename)
            throws KeyStoreException, NoSuchAlgorithmException,
            IOException, CertificateException{

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, password);

        FileOutputStream fos = new FileOutputStream(filename);
        keyStore.store(fos, password);
        fos.close();

        return keyStore;
    }

    public static KeyStore loadKeyStore(char[] password, String filename) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

        keyStore.load(new FileInputStream(filename), password);

        return keyStore;
    }

    /**
     * NOT WORKING
     * @param password
     * @param alias
     * @param keyStore
     * @param privateKey
     * @param cert
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public static void storePrivateKey(char[] password, String alias, KeyStore keyStore, PrivateKey privateKey, X509Certificate cert)
            throws KeyStoreException {

        X509Certificate[] certificateChain = new X509Certificate[1];
        certificateChain[0] = cert;

        keyStore.setKeyEntry(alias, privateKey, password, certificateChain);
    }

    public static PrivateKey loadPrivateKey(char[] password, String alias, KeyStore keyStore)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {

        return (PrivateKey) keyStore.getKey(alias, password);
    }
}
