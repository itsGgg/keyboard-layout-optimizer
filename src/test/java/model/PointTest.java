package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PointTest {

    @Test
    void getX_shouldReturnCorrectCoordinate() {
        Point a = new Point(1, 5);
        assertEquals(1, a.getX());
    }

    @Test
    void getY_shouldReturnCorrectCoordinate() {
        Point a = new Point(1, 5);
        assertEquals(5, a.getY());
    }

    @Test
    void isEqual_shouldReturnTrue_ifTwoPointsHaveSameCoordinates() {
        Point a = new Point(1, 5);
        Point b = new Point(1, 5);
        assertTrue(a.isEqual(b));
    }

    @Test
    void isEqual_shouldReturnFalse_ifTwoPointsHaveDifferentCoordinates() {
        Point a = new Point(1, 5);
        Point b = new Point(3, 5);
        assertFalse(a.isEqual(b));
    }

    @Test
    void distance_shouldReturnCorrectDistance_ifPointsAreDifferent() {
        Point a = new Point(1, 2);
        Point b = new Point(4, 6);
        assertEquals(5.0, a.distance(b));
    }

    @Test
    void distance_shouldReturnZero_ifPointsAreTheSame() {
        Point a = new Point(2, 2);
        Point b = new Point(2, 2);
        assertEquals(0.0, a.distance(b));
    }

}