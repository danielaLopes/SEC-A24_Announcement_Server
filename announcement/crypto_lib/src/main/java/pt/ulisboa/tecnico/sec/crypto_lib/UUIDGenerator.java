package pt.ulisboa.tecnico.sec.crypto_lib;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;

public class UUIDGenerator {

    public static int generateUUID() {
        String uuid = UUID.randomUUID().toString();
        String time = Instant.now().toString();
        String announcementId = uuid + "-" + time;
        int encodedAnnouncementId = -1;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            encodedAnnouncementId = digest.digest(
                    announcementId.getBytes(StandardCharsets.UTF_8)).hashCode();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Wrong algorithm to generate message digest");
        }

        return encodedAnnouncementId;
    }
}
