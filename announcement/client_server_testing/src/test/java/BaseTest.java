public class BaseTest {

    public final static String MESSAGE1 = "message1";
    public final static String MESSAGE2 = "message2";
    public final static String MESSAGE3 = "message3";

    public final static String INVALID_LENGTH_MESSAGE = new String(new char[256]).replace('\0', 'A');

    public final static int MAX_MESSAGE_LENGTH = 255;

    public final static char[] SERVER_KEYSTORE_PASSWD = {'p','a','s','s','w','o','r','d'};
    public final static char[] SERVER_ENTRY_PASSWD = {'p','a','s','s','w','o','r','d'};
    public final static String CLIENT_KEYSTORE_PASSWD = "password";
    public final static String CLIENT_ENTRY_PASSWD = "password";
    public final static String ALIAS = "alias";

    public final static String SERVER_PUBLIC_KEY_PATH = "../server/src/main/resources/crypto/public.key";
    public final static String SERVER_KEYSTORE_PATH = "../server/src/main/resources/crypto/server_keystore.jks";

    public final static String PUBLICKEY_PATH1 = "../client/src/main/resources/crypto/public1.key";
    public final static String KEYSTORE_PATH1 = "../client/src/main/resources/crypto/client1_keystore.jks";

    public final static String PUBLICKEY_PATH2 = "../client/src/main/resources/crypto/public2.key";
    public final static String KEYSTORE_PATH2 = "../client/src/main/resources/crypto/client2_keystore.jks";

    public final static String PUBLICKEY_PATH3 = "../client/src/main/resources/crypto/public3.key";
    public final static String KEYSTORE_PATH3 = "../client/src/main/resources/crypto/client3_keystore.jks";
}