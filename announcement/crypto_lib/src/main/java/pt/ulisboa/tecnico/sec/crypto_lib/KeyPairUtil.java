package pt.ulisboa.tecnico.sec.crypto_lib;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyPairUtil {

    public static KeyPair generateKeyPair(String algorithm, int size) {
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Wrong algorithm to generate key pair");
        }
        keyGen.initialize(size);
        return keyGen.generateKeyPair();
    }

    public static PrivateKey loadPrivateKey(String filename) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        System.out.println(spec);

        return (PrivateKey)kf.generatePrivate(spec);
    }

    public static PublicKey loadPublicKey(String filename) throws IOException,
            NoSuchAlgorithmException, InvalidKeySpecException {

        String keyStr = new String(Files.readAllBytes(Paths.get(filename)));
        PemObject pem = new PemReader(new StringReader(keyStr)).readPemObject();
        byte[] pubKeyBytes = pem.getContent();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return (PublicKey)kf.generatePublic(spec);
    }

    public static X509Certificate loadCertificate(String filename) throws  CertificateException, IOException {

        File file = new File(filename);
        FileInputStream in = new FileInputStream(file);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert =(X509Certificate)cf.generateCertificate(in);
        in.close();

        //c.checkValidity();
        return cert;
    }
}
