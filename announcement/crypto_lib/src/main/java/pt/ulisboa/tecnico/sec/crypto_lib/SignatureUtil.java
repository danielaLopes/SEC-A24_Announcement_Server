package pt.ulisboa.tecnico.sec.crypto_lib;

import java.security.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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

    public static byte[] encrypt(byte[] toSign, PublicKey publicKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
                    NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(toSign);
    }

    public static byte[] decrypt(byte[] toSign, PrivateKey privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException,
                    NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(toSign);
    }

}
