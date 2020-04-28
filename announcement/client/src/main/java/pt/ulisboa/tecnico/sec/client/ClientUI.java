package pt.ulisboa.tecnico.sec.client;

import pt.ulisboa.tecnico.sec.communication_lib.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.util.regex.Pattern;

public class ClientUI {

    private Client _client;
    private Scanner _scanner;

    public ClientUI(String pubKeyPath, String keyStorePath,
                    String keyStorePasswd, String entryPasswd, String alias,
                    int nServers, List<String> otherUsersPubKeyPaths) {
        _client = new Client(pubKeyPath, keyStorePath, keyStorePasswd, entryPasswd,
                alias, nServers, otherUsersPubKeyPaths);
        _scanner = new Scanner(System.in);
    }

    /**
     * Starts Client UI for better interaction.
     */
    public void start() {
        String option = "1";

        while (!option.equals("0")) {
            option = promptGeneralMenu();
            if (!option.equals("\n")) {
                switch (option) {
                    case "1":
                        post();
                        break;
                    // Post to General Board
                    case "2":
                        postGeneral();
                        break;
                    // Read from specific user
                    case "3":
                        read();
                        break;
                    // Read from General Board
                    case "4":
                        readGeneral();
                        break;
                    // Exit and close communication
                    case "0":
                        closeCommunication();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Prompts the user for the message and reference(s) needed to post
     * to his Board.
     */
    public void closeCommunication() {
        try {
            //_client.closeCommunication();
            _client.closeGroupCommunication();
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
        String referencesIn = promptReference();
        if (!referencesIn.trim().equals("") && !Pattern.matches("[\\w+,]*\\w+", referencesIn)) {
            System.out.println("Invalid references sequence.");
            return;
        }

        List<String> references = parseReferences(referencesIn);

        //StatusCode statusCode = _client.post(message, references);
        List<StatusCode> statusCodes = _client.postServersGroup(message, references);
        for (int i = 0; i < _client._nServers; i++) {
            if (statusCodes.get(i) == StatusCode.OK) {
                System.out.println("Posted announcement in server " + (i + 1) + ".");
            }
            else {
                System.out.println("Could not post announcement in server " + (i + 1) + ".");
            }
            _client.printStatusCode(statusCodes.get(i));
        }
    }

    /**
     * Prompts the user for the message and reference(s) needed to post
     * to the General Board.
     */
    public void postGeneral() {
        String message = promptMessage();
        String referencesIn = promptReference();
        if (!referencesIn.trim().equals("") && !Pattern.matches("[\\d+,]*\\d+", referencesIn)) {
            System.out.println("Invalid references sequence.");
            return;
        }
        List<String> references = parseReferences(referencesIn);

        List<StatusCode> statusCodes = _client.postGeneralServersGroup(message, references);
        for (int i = 0; i < _client._nServers; i++) {
            if (statusCodes.get(i) == StatusCode.OK) {
                System.out.println("Posted announcement " + (i + 1) + ".");
            } else {
                System.out.println("Could not post announcement " + (i + 1) + ".");
            }
            _client.printStatusCode(statusCodes.get(i));
        }
    }

    /**
     * Prompts the user for an existing user's ID and for a number n
     * in order to retrieve the n latest announcements.
     */
    public void read() {
        int user = promptUser();
        int number = promptNumber();
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client.read(user, number);
        if (response.getKey() == StatusCode.OK) {
            printAnnouncements(response.getValue(), "USER");
        }
        else {
            System.out.println("Could not read announcements.");
        }
        
    }

    /**
     * Prompts the user for a number n in order to retrieve the General
     * Board's n latest announcements.
     */
    public void readGeneral() {
        int number = promptNumber();
        _client.readGeneral(number);
        AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client.readGeneral(number);
        if (response.getKey() == StatusCode.OK) {
            printAnnouncements(response.getValue(), "GENERAL");
        }
        else {
            System.out.println("Could not read announcements.");
        }
    }

    /**
     * Prints a list of announcements.
     * @param announcements list of announcements to print
     * @param board where the announcements were posted
     */
    public void printAnnouncements(List<Announcement> announcements, String board) {
        if (announcements.size() == 0)
            System.out.println("\nTHERE ARE NO ANNOUNCEMENTS TO DISPLAY");
        else {
            System.out.println("\n-------------- ANNOUNCEMENTS FROM " + board + " --------------");
            for (Announcement a: announcements){
                System.out.println("\n---------------- BEGIN ANNOUNCEMENT ----------------");
                System.out.println("*** From: " + a.getClientPublicKey());
                System.out.println("*** Message: " + a.getAnnouncement());
                System.out.println("*** ID: " + a.getAnnouncementID());
                System.out.println("\n------------------END ANNOUNCEMENT------------------");
            }
        }
    }

    /**
     * Prompts the user for an action.
     */
    public String promptGeneralMenu() {
        System.out.println();
        System.out.println(Message.WELCOME);
        System.out.println("1 - " + Message.POST);
        System.out.println("2 - " + Message.POST_GENERAL);
        System.out.println("3 - " + Message.READ);
        System.out.println("4 - " + Message.READ_GENERAL);
        System.out.println("0 - " + Message.EXIT);

        return _scanner.nextLine();
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

        boolean scan = true;
        int option = 0;
        while (scan) {
            try {
                option = Integer.parseInt(_scanner.nextLine());
                scan = false;
            } catch(NumberFormatException e) {
                scan = true;
            }
        }

        return option;
    }

    /**
     * Prompts the user for a number.
     */
    public int promptNumber() {
        System.out.println(Message.READ_NUMBER);
        boolean scan = true;
        int option = 0;
        while (scan) {
            try {
                option = Integer.parseInt(_scanner.nextLine());
                scan = false;
            } catch(NumberFormatException e) {
                scan = true;
            }
        }

        return option;
    }

    /**
     *
     * @param references describing the ids of the referenced announcements
     *                   separated by commas
     * @return an array of references (one entry for each id)
     */
    public List<String> parseReferences(String references) {
        String[] referencesArray = references.split(",");
        List<String> referencesList = new ArrayList<String>();

        if (!references.equals("")) {
            for (String r : referencesArray) {
                referencesList.add(r.trim());
            }
        }
    
        return referencesList;
    }
}
