import java.util.List;
import java.util.ArrayList;

class BaseTest {

    public final static String MESSAGE = "Hello Server!";
    public final static int MAX_MESSAGE_LENGTH = 255;
    public final static List<Integer> REFERENCES = new ArrayList<Integer>();

    public final static String PUBLICKEY_PATH1 = "src/main/resources/crypto/public1.key";
    public final static String KEYSTORE_PATH1 = "src/main/resources/crypto/client1_keystore.jks";
    
    public final static String PUBLICKEY_PATH2 = "src/main/resources/crypto/public2.key";
    public final static String KEYSTORE_PATH2 = "src/main/resources/crypto/client2_keystore.jks";
    
    public final static String PUBLICKEY_PATH3 = "src/main/resources/crypto/public3.key";
    public final static String KEYSTORE_PATH3 = "src/main/resources/crypto/client3_keystore.jks";
    
    
    public final static String KEYSTORE_PASSWD = "password";
    public final static String ENTRY_PASSWD = "password";
    public final static String ALIAS = "ola";

}