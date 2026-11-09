package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KeyTest {

    @Test
    void isCorrectKey_shouldReturnTrue_whenAllFieldsAreValid() {
        Key key = new Key(Hand.LEFT, Finger.INDEX, new Point(1, 2));
        assertTrue(key.isCorrectKey());
    }

    @Test
    void isCorrectKey_shouldReturnFalse_whenCoordinatesIsNull() {
        Key key = new Key(Hand.RIGHT, Finger.MIDDLE, null);
        assertFalse(key.isCorrectKey());
    }

    @Test
    void isCorrectKey_shouldReturnFalse_whenFingerIsNull() {
        Key key = new Key(Hand.LEFT, null, new Point(1, 2));
        assertFalse(key.isCorrectKey());
    }

    @Test
    void isCorrectKey_shouldReturnFalse_whenHandIsNull() {
        Key key = new Key(null, Finger.PINKY, new Point(1, 2));
        assertFalse(key.isCorrectKey());
    }

    @Test
    void isCorrectKey_shouldReturnFalse_whenAllIsNull() {
        Key key = new Key(null, null, null);
        assertFalse(key.isCorrectKey());
    }

    @Test
    void getHand_shouldReturnCorrectHand() {
        Key key = new Key(Hand.RIGHT, Finger.INDEX, new Point(1, 2));
        assertEquals(Hand.RIGHT, key.getHand());
    }

    @Test
    void getFinger_shouldReturnCorrectFinger() {
        Key key = new Key(Hand.LEFT, Finger.RING, new Point(1, 2));
        assertEquals(Finger.RING, key.getFinger());
    }

    @Test
    void getCoordinates_shouldReturnCorrectCoordinates() {
        Point coordinates = new Point(5, 6);
        Key key = new Key(Hand.LEFT, Finger.INDEX, coordinates);
        assertEquals(coordinates, key.getCoordinates());
    }
}