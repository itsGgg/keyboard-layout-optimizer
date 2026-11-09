package model;

public record Point(int x, int y) {
    @Override
    public String toString() {
        return "[x=" + x + ", y=" + y + "]";
    }

    /**
     * Calcule la distance par rapport à un point
     * 
     * @param p1 le deuxième point
     * @return la distance entre l'instance et p1
     */
    public double distance(Point p1) {
        int newX = this.x - p1.getX();
        int newY = this.y - p1.getY();
        return Math.sqrt(newX * newX + newY * newY);
    }

    /**
     * Vérifie si deux points ont la même position
     * 
     * @param p1 le deuxième point
     * @return si deux points sont égaux
     */
    public boolean isEqual(Point p1) {
        return this.x == p1.getX() && this.y == p1.getY();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
