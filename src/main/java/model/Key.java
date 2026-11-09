package model;

/**
 * Classe répresentant une touche du clavier
 */
public record Key(Hand hand, Finger finger, Point coordinates) {
    public Key(Hand hand, Finger finger, Point coordinates) {
        this.hand = hand;
        this.finger = finger;
        this.coordinates = (coordinates != null) ? new Point(coordinates.getX(), coordinates.getY()) : null;
    }

    @Override
    public String toString() {
        return "Key{coordintares=" + coordinates + ", finger=" + finger + ", hand=" + hand + "}";

    }

    public boolean isCorrectKey() {
        if (coordinates == null || finger == null || hand == null)
            return false;
        return true;
    }

    public Hand getHand() {
        return hand;
    }

    public Finger getFinger() {
        return finger;
    }

    public Point getCoordinates() {
        return coordinates;
    }
}
