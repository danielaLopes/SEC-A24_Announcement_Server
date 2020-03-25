package pt.ulisboa.tecnico.sec.crypto_lib;

public class CreateKeyStorage {
    public static void main(String args[]) {
        if(args.length != 2) {
            System.out.println("This program creates a new keystore.");
            System.out.println("Usage: CreateKeyStorage <keyStorePassword> <keyStore.jks>");
            return;
        }

        try {
            KeyStorage.createKeyStore(args[0].toCharArray(), args[1]);
        } catch (Exception e) {
            System.out.println("Error: Not possible to create new keystore.");
            e.printStackTrace();
            return;
        }

    }
}
