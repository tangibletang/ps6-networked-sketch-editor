import java.awt.*;
import java.util.TreeMap;

public class Sketch {
    private TreeMap<Integer, Shape> shapes;
    private int nextID = 0;
    public Sketch() {
        shapes = new TreeMap<>();
    }
    public synchronized int addShapeWithID(Shape shape) {
        int id = nextID++;
        shapes.put(id, shape);
        return id;
    }
        /**
         * Add a shape with the given ID
         */
    public synchronized void addShape(int id, Shape shape) {
        shapes.put(id, shape);
    }

    /**
     * Delete the shape with the given ID
     */
    public synchronized void deleteShape(int id) {
        shapes.remove(id);
    }

    /**
     * Recolor the shape with the given ID
     */
    public synchronized void recolorShape(int id, Color color) {
        Shape shape = shapes.get(id);
        if (shape != null) {
            shape.setColor(color);
        }
    }

    /**
     * Move the shape with the given ID
     */
    public synchronized void moveShape(int id, int dx, int dy) {
        Shape shape = shapes.get(id);
        if (shape != null) {
            shape.moveBy(dx, dy);
        }
    }

    /**
     * Find the topmost shape at this point (most recently drawn)
     * Returns the shape, or null if no shape contains the point
     */
    public synchronized Shape getShapeAt(int x, int y) {
        // Iterate from highest ID (newest) to lowest (oldest)
        for (Integer id : shapes.descendingKeySet()) {
            Shape shape = shapes.get(id);
            if (shape.contains(x, y)) {
                return shape;
            }
        }
        return null;
    }

    /**
     * Find the ID of the topmost shape at this point
     * Returns the ID, or null if no shape contains the point
     */
    public synchronized Integer getShapeIdAt(int x, int y) {
        System.out.println("getShapeIdAt(" + x + ", " + y + ") - checking " + shapes.size() + " shapes");

        // Iterate from highest ID (newest) to lowest (oldest)
        for (Integer id : shapes.descendingKeySet()) {
            Shape shape = shapes.get(id);
            System.out.println("  Checking shape ID " + id + ": " + shape.getClass().getSimpleName());
            if (shape.contains(x, y)) {
                System.out.println("    -> FOUND!");
                return id;
            }
        }
        System.out.println("  -> No shape found");
        return null;
    }

    /**
     * Get the shape with the given ID
     */
    public synchronized Shape getShape(int id) {
        return shapes.get(id);
    }

    /**
     * Draw all shapes in the sketch
     */
    public synchronized void draw(Graphics g) {
        // Draw from lowest ID (oldest) to highest (newest)
        // So newer shapes appear on top
        for (Shape shape : shapes.values()) {
            shape.draw(g);
        }
    }

    /**
     * Get all shapes (useful for debugging or iteration)
     */
    public synchronized TreeMap<Integer, Shape> getShapes() {
        return new TreeMap<>(shapes);  // Return a copy for safety
    }
}
