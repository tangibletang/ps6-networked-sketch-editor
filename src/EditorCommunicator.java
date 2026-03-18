import java.awt.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2025
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			//Socket sock = new Socket(serverIP, 4242);
			Socket sock = new Socket();
			sock.connect(new InetSocketAddress(serverIP, 4242), 2000);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			// Handle messages
			// TODO: YOUR CODE HERE
            String line;
            while ((line = in.readLine()) != null) {
                handleMessage(line);
            }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}	

	// Send editor requests to the server
	// TODO: YOUR CODE HERE
    private void handleMessage(String message) {
        System.out.println("received: " + message);

        String[] tokens = message.split(" ");
        String command = tokens[0];

        if (command.equals("add")) {
            // Check if this is from server (has ID) or echoed from client (no ID)
            int id;
            String shapeType;
            int startIdx;

            // Try to parse tokens[1] as an integer (ID from server)
            try {
                id = Integer.parseInt(tokens[1]);
                shapeType = tokens[2];
                startIdx = 3;
            } catch (NumberFormatException e) {
                // tokens[1] is not a number, so it's the shape type (from echo)
                id = 0;  // Use dummy ID for echo testing
                shapeType = tokens[1];
                startIdx = 2;
            }

            Shape shape = parseShape(shapeType, tokens, startIdx);
            if (shape != null) {
                editor.getSketch().addShape(id, shape);
                editor.repaint();
            }
        }
        else if (command.equals("delete")) {
            int id = Integer.parseInt(tokens[1]);
            editor.getSketch().deleteShape(id);
            editor.repaint();
        }
        else if (command.equals("recolor")) {
            int id = Integer.parseInt(tokens[1]);
            int colorRGB = Integer.parseInt(tokens[2]);
            Color color = new Color(colorRGB);
            editor.getSketch().recolorShape(id, color);
            editor.repaint();
        }
        else if (command.equals("move")) {
            int id = Integer.parseInt(tokens[1]);
            int dx = Integer.parseInt(tokens[2]);
            int dy = Integer.parseInt(tokens[3]);
            editor.getSketch().moveShape(id, dx, dy);
            editor.repaint();
        }
    }
    private Shape parseShape(String shapeType, String[] tokens, int startIdx) {
        try {
            if (shapeType.equals("ellipse")) {
                // Format: x1 y1 x2 y2 colorRGB
                int x1 = Integer.parseInt(tokens[startIdx]);
                int y1 = Integer.parseInt(tokens[startIdx + 1]);
                int x2 = Integer.parseInt(tokens[startIdx + 2]);
                int y2 = Integer.parseInt(tokens[startIdx + 3]);
                int colorRGB = Integer.parseInt(tokens[startIdx + 4]);
                return new Ellipse(x1, y1, x2, y2, new Color(colorRGB));
            }
            else if (shapeType.equals("rectangle")) {
                // Format: x1 y1 x2 y2 colorRGB
                int x1 = Integer.parseInt(tokens[startIdx]);
                int y1 = Integer.parseInt(tokens[startIdx + 1]);
                int x2 = Integer.parseInt(tokens[startIdx + 2]);
                int y2 = Integer.parseInt(tokens[startIdx + 3]);
                int colorRGB = Integer.parseInt(tokens[startIdx + 4]);
                return new Rectangle(x1, y1, x2, y2, new Color(colorRGB));
            }
            else if (shapeType.equals("segment")) {
                // Format: x1 y1 x2 y2 colorRGB
                int x1 = Integer.parseInt(tokens[startIdx]);
                int y1 = Integer.parseInt(tokens[startIdx + 1]);
                int x2 = Integer.parseInt(tokens[startIdx + 2]);
                int y2 = Integer.parseInt(tokens[startIdx + 3]);
                int colorRGB = Integer.parseInt(tokens[startIdx + 4]);
                return new Segment(x1, y1, x2, y2, new Color(colorRGB));
            }
            else if (shapeType.equals("polyline")) {
                // Format: colorRGB x1 y1 x2 y2 x3 y3 ...
                int colorRGB = Integer.parseInt(tokens[startIdx]);
                Color color = new Color(colorRGB);

                // First point
                int x = Integer.parseInt(tokens[startIdx + 1]);
                int y = Integer.parseInt(tokens[startIdx + 2]);
                Polyline polyline = new Polyline(x, y, color);

                // Remaining points
                for (int i = startIdx + 3; i < tokens.length; i += 2) {
                    x = Integer.parseInt(tokens[i]);
                    y = Integer.parseInt(tokens[i + 1]);
                    polyline.AddPoint(new Point(x,y));
                }
                return polyline;
            }
        }
        catch (Exception e) {
            System.err.println("Error parsing shape: " + e.getMessage());
        }
        return null;
    }
    /**
     * Request to add a new shape
     */
    public void requestAdd(Shape shape) {
        // Determine shape type and format message
        String message;
        if (shape instanceof Ellipse) {
            message = "add " + shape.toString();
        }
        else if (shape instanceof Rectangle) {
            message = "add " + shape.toString();
        }
        else if (shape instanceof Segment) {
            message = "add " + shape.toString();
        }
        else if (shape instanceof Polyline) {
            message = "add " + shape.toString();
        }
        else {
            return;  // Unknown shape type
        }

        send(message);
    }

    /**
     * Request to delete a shape
     */
    public void requestDelete(int id) {
        send("delete " + id);
    }

    /**
     * Request to recolor a shape
     */
    public void requestRecolor(int id, Color color) {
        send("recolor " + id + " " + color.getRGB());
    }

    /**
     * Request to move a shape
     */
    public void requestMove(int id, int dx, int dy) {
        send("move " + id + " " + dx + " " + dy);
    }
}

