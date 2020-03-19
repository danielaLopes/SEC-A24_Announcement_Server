package pt.ulisboa.tecnico.sec.client;

import java.util.Scanner;

public class ClientUI {

    private Client _client;
    private Scanner _scanner;

    public ClientUI() {
        _client = new Client();
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

            switch (option) {
                // Post to Client's Board
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
                default:
                    break;
            }
        }
        _client.closeCommunication();
    }


    /**
     * Prompts the user for the message and reference(s) needed to post
     * to his Board.
     */
    public void post() {
        String message = promptMessage();
        String reference = promptReference();
        _client.post(message, reference);
    }

    /**
     * Prompts the user for the message and reference(s) needed to post
     * to the General Board.
     */
    public void postGeneral() {
        String message = promptMessage();
        String reference = promptReference();
        _client.postGeneral(message, reference);
    }

    /**
     * Prompts the user for an existing user's ID and for a number n
     * in order to retrieve the n latest announcements.
     */
    public void read() {
        String user = promptUser();
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

        return Integer.parseInt(_scanner.nextLine());
    }

    /**
     * Prompts the user for a message.
     */
    public String promptMessage() {
        System.out.print(Message.MESSAGE);
        return _scanner.nextLine();
    }

    /**
     * Prompts the user for references.
     */
    public String promptReference() {
        System.out.print(Message.REFERENCE);
        return _scanner.nextLine();
    }

    /**
     * Prompts the user for another user's ID.
     */
    public String promptUser() {
        System.out.print(Message.USER_BOARD);
        return _scanner.nextLine();
    }

    /**
     * Prompts the user for a number.
     */
    public int promptNumber() {
        System.out.print(Message.READ_NUMBER);
        return Integer.parseInt(_scanner.nextLine());
    }
}
