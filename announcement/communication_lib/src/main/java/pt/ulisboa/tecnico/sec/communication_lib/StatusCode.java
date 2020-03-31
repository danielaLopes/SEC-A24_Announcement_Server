package pt.ulisboa.tecnico.sec.communication_lib;

public enum StatusCode {
    OK(0, "Successful."),
    USER_ALREADY_REGISTERED(1, "User is already registered."),
    INVALID_KEY(2, "Public Key is invalid."),
    INVALID_SIGNATURE(3, "Signature is invalid, message might be tampered or signed with another private key."),
    DUPLICATE_OPERATION(4, "Operation with given UUID was already processed."),
    INVALID_MESSAGE_LENGTH(5, "Maximum message length to post announcement is 255."),
    USER_NOT_REGISTERED(6, "User is not registed."),
    INVALID_REFERENCE(7, "Invalid Reference: referenced announcement does not exist."),
    INVALID_ALGORITHM(8, "Signature Algorithm not supported"),
    NULL_FIELD(9, "Null fields are not allowed."),
    INVALID_COMMAND(10, "Requested command is not supported.");

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
