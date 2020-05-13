package pt.ulisboa.tecnico.sec.client;

import java.security.PublicKey;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import pt.ulisboa.tecnico.sec.communication_lib.Announcement;
import pt.ulisboa.tecnico.sec.communication_lib.StatusCode;

public class ClientUI {

    private Client _client;
    private Scanner _scanner;

    public ClientUI(String pubKeyPath, String keyStorePath,
                    String keyStorePasswd, String entryPasswd, String alias,
                    int nServers, int nFaults, List<String> otherUsersPubKeyPaths) {
        _client = new Client(pubKeyPath, keyStorePath, keyStorePasswd, entryPasswd,
                alias, nServers, nFaults, otherUsersPubKeyPaths, this);
        _scanner = new Scanner(System.in);
    }

    /**
     * Starts Client UI for better interaction.
     */
    public void start() {

        boolean repeat = false;
        //do {
            String option = promptGeneralMenu();
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
                        // System.exit(0);
                        break;
                    default:
                        repeat = true;
                        break;
                }
           }
        //} while(repeat == true);
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
        _client.post(message, references);
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

        _client.postGeneral(message, references);
    }

    public void deliverPost(StatusCode sc) {
        if (sc.equals(StatusCode.NO_CONSENSUS))
            System.out.println("(INFO) No quorum: Could not post announcement in user board.");
        else
            _client.printStatusCode("POST", sc);
        start();
    }

    public void deliverPostGeneral(StatusCode sc) {
        if (sc.equals(StatusCode.NO_CONSENSUS))
            System.out.println("(INFO) No quorum: Could not post announcement in general board.");
        else
            _client.printStatusCode("POSTGENERAL", sc);
        start();
    }

    public void deliverRead(StatusCode sc, List<Announcement> announcements) {
        if (sc.equals(StatusCode.NO_CONSENSUS))
            System.out.println("(INFO) No quorum: Could not read announcements from user board.");
        else if (sc.equals(StatusCode.USER_NOT_REGISTERED))
            System.out.println("(INFO) User is still not registered. No announcements to display.");
        else
            _client.printStatusCode("READ", sc);
        if(sc == StatusCode.OK)
            printAnnouncements(announcements, "USER");
        start();
    }

    public void deliverReadGeneral(StatusCode sc, List<Announcement> announcements) {
        if (sc.equals(StatusCode.NO_CONSENSUS))
            System.out.println("(INFO) No quorum: Could not read announcements from general board.");
        else
            _client.printStatusCode("READGENERAL",sc);
        if(sc == StatusCode.OK)
            printAnnouncements(announcements, "USER");
        start();
    }

    /**
     * Prompts the user for an existing user's ID and for a number n
     * in order to retrieve the n latest announcements.
     */
    public void read() {
        int user = promptUser();
        int number = promptNumber();

        if (invalidUser(user)) {
            System.out.println("Invalid user.");
            return;
        }
        PublicKey userToReadPB = _client._usersPubKeys.get(user);

        //AbstractMap.SimpleEntry<StatusCode, List<Announcement>> response = _client.read(user, number);
        _client.read(userToReadPB, number);
        
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
     * Verifies if a user exists within _usersPubKeys.
     * @param user
     */
    public boolean invalidUser(int user) {
        return user < 0 || user >= _client._usersPubKeys.size();
    }

    /**
     * Prints a list of announcements.
     * @param announcements list of announcements to print
     * @param board where the announcements were posted
     */
    public void printAnnouncements(List<Announcement> announcements, String board) {
        if (announcements.size() == 0)
            System.out.println("\n-------------- THERE ARE NO ANNOUNCEMENTS TO DISPLAY --------------");
        else {
            System.out.println("\n-------------- ANNOUNCEMENTS FROM " + board + " --------------");
            for (Announcement a: announcements){
                System.out.println("\n---------------- BEGIN ANNOUNCEMENT ----------------");
                System.out.println("*** From: " + a.getClientPublicKey().toString().substring(0, 120) + "...");
                System.out.println("*** Message: " + a.getAnnouncement());
                System.out.println("*** ID: " + a.getAnnouncementID());
                System.out.println("\n------------------END ANNOUNCEMENT------------------");
                System.out.flush();
            }
        }
    }

    /**
     * Prompts the user for an action.
     */
    public String promptGeneralMenu() {
        try{
            System.out.println();
            System.out.println(Message.WELCOME);
            System.out.println("1 - " + Message.POST);
            System.out.println("2 - " + Message.POST_GENERAL);
            System.out.println("3 - " + Message.READ);
            System.out.println("4 - " + Message.READ_GENERAL);
            System.out.println("0 - " + Message.EXIT);

            String cmd = _scanner.nextLine();
            return cmd;
        }
        catch(IndexOutOfBoundsException e) {
            return "";
        }
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
