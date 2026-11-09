package model.exceptions;

public final class InvalidExtensionException extends InvalidFileException {
    public InvalidExtensionException(String invalidName, Entry entry) {
        super(invalidName, entry);
    }
}
