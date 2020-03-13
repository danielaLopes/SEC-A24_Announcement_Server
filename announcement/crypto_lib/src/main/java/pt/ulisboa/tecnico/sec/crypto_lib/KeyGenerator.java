package pt.ulisboa.tecnico.sec.crypto_lib;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyGenerator {

    public KeyPair generateKeyPair(String algorithm, int size) {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Wrong algorithm to generate key pair");
        }
        keyGen.initialize(size);
        return keyGen.generateKeyPair();
    }
}
