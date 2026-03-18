import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2025
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client
	private PrintWriter out;				// to client
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
            synchronized (server.getSketch()) {
                for (Integer id : server.getSketch().getShapes().keySet()) {
                    Shape shape = server.getSketch().getShape(id);
                    send("add " + id + " " + shape.toString());
                }
            }
			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("received from client: " + line);
                handleRequest(line);
            }
			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

    private void handleRequest(String request) {
        String[] tokens = request.split(" ");
        String command = tokens[0];

        if (command.equals("add")) {
            // Format: "add <shapeType> <params...>"
            // Parse the shape type
            String shapeType = tokens[1];

            // Parse the shape from tokens
            Shape shape = parseShape(shapeType, tokens, 2);

            if (shape != null) {
                // Add to master sketch and get the new ID
                int id = server.getSketch().addShapeWithID(shape);

                // Broadcast to all clients (including this one)
                server.broadcast("add " + id + " " + shape.toString());
            }
        }
        else if (command.equals("delete")) {
            // Format: "delete <id>"
            int id = Integer.parseInt(tokens[1]);

            // Delete from master sketch
            server.getSketch().deleteShape(id);

            // Broadcast to all clients
            server.broadcast("delete " + id);
        }
        else if (command.equals("recolor")) {
            // Format: "recolor <id> <colorRGB>"
            int id = Integer.parseInt(tokens[1]);
            int colorRGB = Integer.parseInt(tokens[2]);
            Color color = new Color(colorRGB);

            // Recolor in master sketch
            server.getSketch().recolorShape(id, color);

            // Broadcast to all clients
            server.broadcast("recolor " + id + " " + colorRGB);
        }
        else if (command.equals("move")) {
            // Format: "move <id> <dx> <dy>"
            int id = Integer.parseInt(tokens[1]);
            int dx = Integer.parseInt(tokens[2]);
            int dy = Integer.parseInt(tokens[3]);

            // Move in master sketch
            server.getSketch().moveShape(id, dx, dy);

            // Broadcast to all clients
            server.broadcast("move " + id + " " + dx + " " + dy);
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
                    polyline.AddPoint(new Point(x, y));
                }
                return polyline;
            }
        }
        catch (Exception e) {
            System.err.println("Error parsing shape: " + e.getMessage());
        }
        return null;
    }
}
