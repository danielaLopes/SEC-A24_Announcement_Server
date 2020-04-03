import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyStorage;
import pt.ulisboa.tecnico.sec.database_lib.Database;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

class BaseTest {

    public final static String MESSAGE = "Hello Server!";
    public final static int MAX_MESSAGE_LENGTH = 255;
    public final static List<String> REFERENCES = new ArrayList<String>();

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
    public final static String ALIAS = "alias";

    public PrivateKey loadPrivateKey(String keyStorePath, String keyStorePasswd, String entryPasswd, String alias) throws Exception {
        if (keyStorePath == null || keyStorePasswd == null || entryPasswd == null || alias == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load keystore.\n");
            return null;
        }

        PrivateKey privateKey = null;
        KeyStore keyStore = null;
        keyStore = KeyStorage.loadKeyStore(keyStorePasswd.toCharArray(), keyStorePath);
        privateKey = KeyStorage.loadPrivateKey(entryPasswd.toCharArray(), alias, keyStore);
        
        return privateKey;
    }

    public PublicKey loadPublicKey(String pubKeyPath) throws Exception {
        if (pubKeyPath == null) {
            System.out.println("Error: Not possible to initialize client because it was not possible to load public key.\n");
            return null;
        }
        return KeyPairUtil.loadPublicKey(pubKeyPath);
    }

}