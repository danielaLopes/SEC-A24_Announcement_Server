import org.junit.jupiter.api.BeforeAll;
import pt.ulisboa.tecnico.sec.crypto_lib.KeyPairUtil;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class BaseTest {

        public final static String MESSAGE1 = "message1";
        public final static String MESSAGE2 = "message2";
        public final static String MESSAGE3 = "message3";

        public final static String INVALID_LENGTH_MESSAGE = new String(new char[256]).replace('\0', 'A');;

        public final static int MAX_MESSAGE_LENGTH = 255;

        public final static char[] KEYSTORE_PASSWD = {'p','a','s','s','w','o','r','d'};
        public final static char[] ENTRY_PASSWD = {'p','a','s','s','w','o','r','d'};
        public final static String ALIAS = "alias";

        public final static String SERVER_PUBLIC_KEY_PATH = "src/main/resources/crypto/public.key";
        public final static String SERVER_KEYSTORE_PATH = "src/main/resources/crypto/server_keystore.jks";

        public static PublicKey CLIENT1_PUBLIC_KEY;
        public static PrivateKey CLIENT1_PRIVATE_KEY;
        public static PublicKey CLIENT2_PUBLIC_KEY;
        public static PrivateKey CLIENT2_PRIVATE_KEY;
        public static PublicKey CLIENT3_PUBLIC_KEY;
        public static PrivateKey CLIENT3_PRIVATE_KEY;

        @BeforeAll
        public static void oneTimeSetup() {

                KeyPair client1KeyPair = KeyPairUtil.generateKeyPair("RSA", 2048);
                CLIENT1_PUBLIC_KEY = client1KeyPair.getPublic();
                CLIENT1_PRIVATE_KEY = client1KeyPair.getPrivate();

                KeyPair client2KeyPair = KeyPairUtil.generateKeyPair("RSA", 2048);
                CLIENT2_PUBLIC_KEY = client1KeyPair.getPublic();
                CLIENT2_PRIVATE_KEY = client1KeyPair.getPrivate();

                KeyPair client3KeyPair = KeyPairUtil.generateKeyPair("RSA", 2048);
                CLIENT3_PUBLIC_KEY = client1KeyPair.getPublic();
                CLIENT3_PRIVATE_KEY = client1KeyPair.getPrivate();
        }
}
