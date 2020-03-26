package pt.ulisboa.tecnico.sec.crypto_lib;

import java.security.*;

public class SignatureUtil {

    public static byte[] sign(byte[] toSign, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(toSign);

        return sign.sign();
    }

    public static boolean verifySignature(byte[] signature, PublicKey publicKey, byte[] toVerify)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(publicKey);
        sign.update(toVerify);

        return sign.verify(signature);
    }
}
