import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2025
 * @author Alex Tang Dartmouth CS10 Fall 2025 (the todo section)
 */
public class Polyline implements Shape {
	// TODO: YOUR CODE HERE
    private ArrayList<Point> points;  // Sequence of points along the freehand path
    private Color color;
    public Polyline (int x, int y, Color color){
        this.color = color;
        this.points = new ArrayList<>();
        this.points.add(new Point (x,y));
    }
    public void AddPoint(Point p){
        this.points.add(p);
    }

	public void moveBy(int dx, int dy) {
        for (Point p : points){
            p.x += dx;
            p.y += dy;
        }
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
        // Check if point is near any segment of the polyline
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);

            // Use static method from Segment class
            if (Segment.pointToSegmentDistance(x, y, p1.x, p1.y, p2.x, p2.y) <= 3) {
                return true;
            }
        }
        return false;
    }

	@Override
	public void draw(Graphics g) {
        g.setColor(color);
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
	}

	@Override
	public String toString() {
        StringBuilder result = new StringBuilder("polyline " + color.getRGB());
        for (Point p : points) {
            result.append(" ").append(p.x).append(" ").append(p.y);
        }
        return result.toString();
	}
}
