package pt.ulisboa.tecnico.sec.client;

public enum Message {

    WELCOME("Welcome to DPAS!"),

    POST("Post announcement to your Board"),
    POST_GENERAL("Post announcement to General Board"),
    READ("Read announcements from Users' Boards"),
    READ_GENERAL("Read announcements from General Board"),

    MESSAGE("Message: "),
    REFERENCE("Announcements referenced (format: announcementId1,announcementId2,...): "),
    USER_BOARD("Read announcements from user: "),
    READ_NUMBER("Number of announcements to read: "),

    EXIT("Exit");

    private final String _message;

    private Message(String message) {
        _message = message;
    }

    public String getMessage() {
        return _message;
    }

    @Override
    public String toString() {
        return _message;
    }

}
