package pt.ulisboa.tecnico.sec.crypto_lib;

import java.security.*;

public class SignatureUtil {

    public static byte[] sign(byte[] messageToSign, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(messageToSign);

        return sign.sign();
    }

    public static boolean verifySignature(byte[] signature, PublicKey publicKey, byte[] messagetoVerify)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        sign.update(messagetoVerify);

        return sign.verify(signature);
    }
}
