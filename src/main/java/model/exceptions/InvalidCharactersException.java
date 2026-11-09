package model.exceptions;

import java.util.Set;
import java.util.TreeSet;

public final class InvalidCharactersException extends Exception {
    private final Set<Character> invalidCharacters;

    public InvalidCharactersException(Set<Character> invalidCharacters) {
        this.invalidCharacters = new TreeSet<>(invalidCharacters);
    }

    public String error(String fileName) {
        String result = "Le clavier fourni n'est pas compatible avec le fichier " + fileName + " (";
        int length = invalidCharacters.size();
        int i = 0;
        for (Character c : invalidCharacters) {
            if (i < length - 2)
                result += c + ", ";
            else if (i < length - 1)
                result += c + " et ";
            else
                result += c + " non valide" + (invalidCharacters.size() > 1 ? "s" : "") + ")";
            i++;
        }
        return result;
    }
}
