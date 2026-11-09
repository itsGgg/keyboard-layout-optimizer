package model.exceptions;

public abstract sealed class InvalidFileException extends Exception
        permits InvalidNameException, InvalidExtensionException {
    private final String invalidName;
    private final Entry entry;

    public InvalidFileException(String invalidName, Entry entry) {
        this.invalidName = invalidName;
        this.entry = entry;
    }

    public String getInvalidName() {
        return invalidName;
    }

    public Entry getEntry() {
        return entry;
    }

    public static enum Entry {
        TEXT, KEYBOARD, LAYOUT, CONSTANT
    }
}
