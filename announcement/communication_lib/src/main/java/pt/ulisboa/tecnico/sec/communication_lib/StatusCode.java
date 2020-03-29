package pt.ulisboa.tecnico.sec.communication_lib;

public enum StatusCode {
    OK(0, "Successful."),
    DUPLICATE_USER(1, "User already exists."),
    INVALID_KEY(2, "Public Key is invalid."),
    INVALID_SIGNATURE(3, "Signature is invalid."),
    DUPLICATE_OPERATION(4, "Operation with given UUID was already processed."),
    INVALID_MESSAGE_LENGTH(5, "Maximum message length to post announcement is 255."),
    USER_NOT_REGISTERED(6, "User is not registed."),
    INVALID_REFERENCE(7, "Invalid Reference: referenced announcement does not exist."),
    INVALID_ALGORITHM(8, "Signature Algorithm not supported"),
    USER_ALREADY_REGISTERED(9, "User is already registered.");
    // UNREGISTERED_USER

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
