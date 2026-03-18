import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2025
 * @author Alex Tang Dartmouth CS10 Fall 2025 (filled out TODO sections)
 */
public class Rectangle implements Shape {
	// TODO: YOUR CODE HERE
    private int x1, y1, x2, y2;
    private Color color;

    //constructor
    public Rectangle(int x1, int y1, int x2, int y2, Color color){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
    }

	@Override
	public void moveBy(int dx, int dy) {
        x1 += dx;
        y1 += dy;
        x2 += dx;
        y2 += dy;
	}

	@Override
	public Color getColor() {
        return color;
	}

	@Override
	public void setColor(Color color) {
        this.color = color;
	}
		
	@Override
    public boolean contains(int x, int y) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        boolean result = x >= minX && x <= maxX && y >= minY && y <= maxY;
        System.out.println("Rectangle.contains(" + x + ", " + y + "): bounds=[" + minX + "," + minX + "," + maxX + "," + maxY + "] result=" + result);

        return result;
    }

	@Override
	public void draw(Graphics g) {
        g.setColor(color);

        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        int width = Math.abs(x2 - x1);
        int height = Math.abs(y2 - y1);

        g.fillRect(x, y, width, height);
	}

	public String toString() {
        return "rectangle " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + color.getRGB();
	}
    /**
     * Update the corners (for resizing while drawing)
     */
    public void setCorners(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}
