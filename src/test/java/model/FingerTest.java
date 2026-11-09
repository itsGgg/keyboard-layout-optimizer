package model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FingerTest {

    @Test
    public void isThumb_shouldReturnTrueIfThumb() {
        Finger a = Finger.THUMB;
        assertTrue(a.isThumb());
    }

    @Test
    public void isIndex_shouldReturnTrueIfIndex() {
        Finger a = Finger.INDEX;
        assertTrue(a.isIndex());
    }

    @Test
    public void isMiddle_shouldReturnTrueIfMiddle() {
        Finger a = Finger.MIDDLE;
        assertTrue(a.isMiddle());
    }

    @Test
    public void isRing_shouldReturnTrueIfRing() {
        Finger a = Finger.RING;
        assertTrue(a.isRing());
    }

    @Test
    public void isPinky_shouldReturnTrueIfPinky() {
        Finger a = Finger.PINKY;
        assertTrue(a.isPinky());
    }
}