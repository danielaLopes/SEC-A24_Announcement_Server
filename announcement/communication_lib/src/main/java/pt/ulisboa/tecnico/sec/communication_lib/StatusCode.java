package pt.ulisboa.tecnico.sec.communication_lib;

public enum StatusCode {
    OK(0, "Successful."),
    DUPLICATE_USER(1, "User already exists."),
    INVALID_KEY(2, "Public Key is invalid."),
    INVALID_SIGNATURE(3, "Signature is invalid."),
    DUPLICATE_OPERATION(4, "Operation with given UUID was already processed."),
    INVALID_MESSAGE_LENGTH(5, "Maximum message length to post announcement is 255.");
    //INVALID_NUMBER_OF_ANNOUNCEMENTS(6, ".");

    private final int code;
    private final String description;

    StatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
