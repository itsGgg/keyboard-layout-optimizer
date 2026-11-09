package model;

/**
 * Enum pour les doigts
 */
public enum Finger {
    THUMB, INDEX, MIDDLE, RING, PINKY;

    public boolean isThumb() {
        return this == THUMB;
    }

    public boolean isIndex() {
        return this == INDEX;
    }

    public boolean isMiddle() {
        return this == MIDDLE;
    }

    public boolean isRing() {
        return this == RING;
    }

    public boolean isPinky() {
        return this == PINKY;
    }
}
