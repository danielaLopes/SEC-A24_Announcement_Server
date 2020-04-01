import java.util.List;

import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.ArrayList;

class BaseTest {

    public final static String MESSAGE = "Hello Server!";
    public final static int MAX_MESSAGE_LENGTH = 255;
    public final static List<Integer> REFERENCES = new ArrayList<Integer>();

    public final static String SERVER_PUBLICKEY_PATH = "../server/src/main/resources/crypto/public.key";
    public final static String SERVER_KEYSTORE_PATH1 = "../server/src/main/resources/crypto/server_keystore.jks";
    public final static String SERVER_PASSWD = "password";
    public final static String SERVER_ALIAS = "ola";


    public final static String PUBLICKEY_PATH1 = "src/main/resources/crypto/public1.key";
    public final static String KEYSTORE_PATH1 = "src/main/resources/crypto/client1_keystore.jks";
    
    public final static String PUBLICKEY_PATH2 = "src/main/resources/crypto/public2.key";
    public final static String KEYSTORE_PATH2 = "src/main/resources/crypto/client2_keystore.jks";
    
    public final static String PUBLICKEY_PATH3 = "src/main/resources/crypto/public3.key";
    public final static String KEYSTORE_PATH3 = "src/main/resources/crypto/client3_keystore.jks";
    
    
    public final static String KEYSTORE_PASSWD = "password";
    public final static String ENTRY_PASSWD = "password";
    public final static String ALIAS = "ola";


    public PrivateKey loadPrivateKey(String keyStorePath, String keyStorePasswd, String entryPasswd, String alias) {
        if (keyStorePath == null || keyStorePasswd == null || entryPasswd == null || alias == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load keystore.\n");
            System.exit(-1);
        }

        PrivateKey privateKey = null;
        KeyStore keyStore = null;
        try {
            keyStore = KeyStorage.loadKeyStore(keyStorePasswd.toCharArray(), keyStorePath);
        } catch(Exception e) {
            System.out.println("Error: Not possible to load keystore.\n" + e);
        }
        try {
            privateKey = KeyStorage.loadPrivateKey(entryPasswd.toCharArray(), alias, keyStore);
        } catch (Exception e) {
            System.out.println("Error: Not possible to load private key.\n" + e);
        }
        if (privateKey == null) {
            System.out.println("Error: Private key is null.\n");
        }

        return privateKey;
    }

}