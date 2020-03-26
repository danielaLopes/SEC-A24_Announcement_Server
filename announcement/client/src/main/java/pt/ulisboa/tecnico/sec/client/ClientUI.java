package pt.ulisboa.tecnico.sec.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClientUI {

    private Client _client;
    private Scanner _scanner;

    public ClientUI(String pubKeyPath, String keyStorePath,
                    String keyStorePasswd, String entryPasswd, String alias,
                    List<String> otherUsersPubKeyPaths) {
        _client = new Client(pubKeyPath, keyStorePath, keyStorePasswd, entryPasswd, alias, otherUsersPubKeyPaths);
        _scanner = new Scanner(System.in);
    }

    /**
     * Starts Client UI for better interaction.
     */
    public void start() {
        _client.startServerCommunication();
        int option = 1;

        while (option != 0) {
            option = promptGeneralMenu();
            System.out.println("OPTION:" + option);
            switch (option) {
                case 1:
                    post();
                    break;
                // Post to General Board
                case 2:
                    postGeneral();
                    break;
                // Read from specific user
                case 3:
                    read();
                    break;
                // Read from General Board
                case 4:
                    readGeneral();
                    break;
                // Exit and close communication
                case 0:
                    closeCommunication();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Prompts the user for the message and reference(s) needed to post
     * to his Board.
     */
    public void closeCommunication() {
        try {
            _client.closeCommunication();
        }
        catch(Exception e) {
            System.out.println("Error when closing Client's communications.");
        }
    }

    /**
     * Prompts the user for the message and reference(s) needed to post
     * to his Board.
     */
    public void post() {
        String message = promptMessage();
        List<Integer> references = parseReferences(promptReference());

        _client.postGeneral(message, references);
    }

    /**
     * Prompts the user for the message and reference(s) needed to post
     * to the General Board.
     */
    public void postGeneral() {
        String message = promptMessage();
        List<Integer> references = parseReferences(promptReference());

        _client.postGeneral(message, references);
    }

    /**
     * Prompts the user for an existing user's ID and for a number n
     * in order to retrieve the n latest announcements.
     */
    public void read() {
        int user = promptUser();
        int number = promptNumber();
        _client.read(user, number);
    }

    /**
     * Prompts the user for a number n in order to retrieve the General
     * Board's n latest announcements.
     */
    public void readGeneral() {
        int number = promptNumber();
        _client.readGeneral(number);
    }

    /**
     * Prompts the user for an action.
     */
    public int promptGeneralMenu() {
        System.out.println();
        System.out.println(Message.WELCOME);
        System.out.println("1 - " + Message.POST);
        System.out.println("2 - " + Message.POST_GENERAL);
        System.out.println("3 - " + Message.READ);
        System.out.println("4 - " + Message.READ_GENERAL);
        System.out.println("0 - " + Message.EXIT);

        return _scanner.nextInt();
    }

    /**
     * Prompts the user for a message.
     */
    public String promptMessage() {
        System.out.println(Message.MESSAGE);
        return _scanner.nextLine();
    }

    /**
     * Prompts the user for references.
     */
    public String promptReference() {
        System.out.println(Message.REFERENCE);
        return _scanner.nextLine();
    }

    /**
     * Prompts the user for another user's ID.
     */
    public int promptUser() {
        _client.printOtherUsersPubKeys();
        System.out.println(Message.USER_BOARD);

        return _scanner.nextInt();
    }

    /**
     * Prompts the user for a number.
     */
    public int promptNumber() {
        System.out.println(Message.READ_NUMBER);
        return _scanner.nextInt();
    }

    /**
     *
     * @param references describing the ids of the referenced announcements
     *                   separated by commas
     * @return an array of references (one entry for each id)
     */
    public List<Integer> parseReferences(String references) {
        String[] referencesArray = references.split(",");
        List<Integer> referencesList = new ArrayList<Integer>();

        for (String r : referencesArray) {
            referencesList.add(Integer.parseInt(r));
        }

        return referencesList;
    }
}
