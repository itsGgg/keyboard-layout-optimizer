package model.exceptions;

public final class InvalidNameException extends InvalidFileException {
    public InvalidNameException(String invalidName, Entry entry) {
        super(invalidName, entry);
    }
}
