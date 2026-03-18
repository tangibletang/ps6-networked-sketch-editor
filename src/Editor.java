import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Tim Pierson Dartmouth CS 10, provided for Winter 2025
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}
	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add
	private Color color = Color.black;			// current drawing color

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
		// TODO: YOUR CODE HERE
        // Draw all completed shapes in the sketch (from oldest to newest)
        sketch.draw(g);

        // Draw the current shape being drawn, if there is one
        if (curr != null) {
            curr.draw(g);
        }
	}

	// Helpers for event handlers
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, (request to) start dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
        // TODO: YOUR CODE HERE
        if (mode == Mode.DRAW) {
            drawFrom = p;

            if (shapeType.equals("ellipse")) {
                curr = new Ellipse(p.x, p.y, color);
            } else if (shapeType.equals("rectangle")) {
                curr = new Rectangle(p.x, p.y, p.x, p.y, color);
            } else if (shapeType.equals("segment")) {
                curr = new Segment(p.x, p.y, color);
            } else if (shapeType.equals("freehand")) {
                curr = new Polyline(p.x, p.y, color);
            }
            repaint();
        }else if (mode == Mode.MOVE) {
            //find which shape was clicked
            System.out.println("MOVE mode: clicked at (" + p.x + ", " + p.y + ")");
            Integer shapeId = sketch.getShapeIdAt(p.x, p.y);
            System.out.println("Found shape ID: " + shapeId);
            if (shapeId != null) {
                movingId = shapeId;
                moveFrom = p;
            }
        } else if (mode == Mode.RECOLOR) {
            //find which shape was clicked and request recolor
            Integer shapeId = sketch.getShapeIdAt(p.x, p.y);
            if (shapeId != null) {
                comm.requestRecolor(shapeId, color);
            }
        } else if (mode == Mode.DELETE) {
            // Find which shape was clicked and request delete
            Integer shapeId = sketch.getShapeIdAt(p.x, p.y);
            if (shapeId != null) {
                comm.requestDelete(shapeId);
            }
        }
    }

	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// TODO: YOUR CODE HERE
        if (mode == Mode.DRAW) {
            // Update the current shape being drawn
            if (curr != null) {
                if (shapeType.equals("ellipse")) {
                    ((Ellipse) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
                }
                else if (shapeType.equals("rectangle")) {
                    ((Rectangle) curr).setCorners(drawFrom.x, drawFrom.y, p.x, p.y);
                }
                else if (shapeType.equals("segment")) {
                    ((Segment) curr).setEnd(p.x, p.y);
                }
                else if (shapeType.equals("freehand")) {
                    ((Polyline) curr).AddPoint(p);
                }

                repaint();
            }
        }
        else if (mode == Mode.MOVE) {
            // Request server to move the shape
            if (movingId != -1 && moveFrom != null) {
                int dx = p.x - moveFrom.x;
                int dy = p.y - moveFrom.y;
                comm.requestMove(movingId, dx, dy);
                moveFrom = p;  // Update for next drag event
            }
        }

	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object request on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		// TODO: YOUR CODE HERE
        if (mode == Mode.DRAW) {
            // Finished drawing - request server to add the shape
            if (curr != null) {
                comm.requestAdd(curr);
                curr = null;  // Clear current shape
            }
        }
        else if (mode == Mode.MOVE) {
            // Stop moving
            moveFrom = null;
            movingId = -1;
        }
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
