package Mapper;


import java.awt.Graphics;
import java.awt.Point;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.util.*;
import java.io.*;

public class AucklandMapper {

	private JFrame frame;
	private JComponent drawing;
	private JTextArea textOutput;
	private JTextField nameEntry;
	private int windowSize = 900;

	private RoadGraph roadGraph;

	private Node selectedNode; // the currently selected node
	private Node startNode;
	private Node goalNode;
	private List<Segment> aStarSegs;
	//private List<Segment> seggysegs;
	private List<Node> artPtNodes;
	private List<Segment> selectedSegments; // the currently selected road or
											// path

	private Map <String, Double> strlengths;

	private boolean loaded = false;

	// Dimensions for drawing
	double westBoundary;
	double eastBoundary;
	double southBoundary;
	double northBoundary;
	Location origin;
	double scale;

	public AucklandMapper(String dataDir) {
		setupInterface();
		roadGraph = new RoadGraph();

		textOutput.setText("Loading data...");
		while (dataDir == null) {
			dataDir = getDataDir();
		}
		textOutput.append("Loading from " + dataDir + "\n");
		textOutput.append(roadGraph.loadData(dataDir));
		setupScaling();
		loaded = true;
		drawing.repaint();
	}

	private class DirectoryFileFilter extends FileFilter {
		public boolean accept(File f) {
			return f.isDirectory();
		}

		public String getDescription() {
			return "Directories only";
		}
	}

	private String getDataDir() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new DirectoryFileFilter());
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return fc.getSelectedFile().getPath() + File.separator;
	}

	private void setupScaling() {
		double[] b = roadGraph.getBoundaries();
		westBoundary = b[0];
		eastBoundary = b[1];
		southBoundary = b[2];
		northBoundary = b[3];
		resetOrigin();
		/*
		 * System.out.printf("Boundaries: w %.2f, e %.2f, s %.2f, n %.2f%n",
		 * b[0], b[1], b[2], b[3]);
		 * System.out.printf("Scaling from %s @ %.5f,%n", origin, scale);
		 */
	}

	private void setupInterface() {
		// Set up a window .
		frame = new JFrame("Graphics Example");
		frame.setSize(windowSize, windowSize);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Set up a JComponent in the window that we can draw on
		// When the JComponent tries to paint itself, it will call the redraw
		// method
		// in this PathDrawer class, passing a Graphics object to it.
		// The redraw method can draw whatever it wants on the Graphics object.
		// We can ask the JComponent to paint itself by calling
		// drawing.repaint()
		// Note that this merely requests that the drawing is repainted; it
		// won't
		// necessarily do it immediately.
		drawing = new JComponent() {
			protected void paintComponent(Graphics g) {
				redraw(g);
			}
		};
		frame.add(drawing, BorderLayout.CENTER);

		// Setup a text area for output
		textOutput = new JTextArea(5, 100);
		textOutput.setEditable(false);
		JScrollPane textSP = new JScrollPane(textOutput);
		frame.add(textSP, BorderLayout.SOUTH);

		// Set up a panel for some buttons.
		// To get nicer layout, we would need a LayoutManager on the panel.
		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.NORTH);

		// Add a text label to the panel.
		// panel.add(new JLabel("Click to select"));

		// Add buttons to the panel.
		JButton button = new JButton("+");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				zoomIn();
			}
		});

		button = new JButton("-");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				zoomOut();
			}
		});

		button = new JButton("<");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("left");
			}
		});

		button = new JButton(">");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("right");
			}
		});

		button = new JButton("^");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("up");
			}
		});

		button = new JButton("v");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				pan("down");
			}
		});

		nameEntry = new JTextField(20);
		panel.add(nameEntry);
		nameEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lookupName(nameEntry.getText());
				drawing.repaint();
			}
		});

		button = new JButton("Quit");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				System.exit(0);
			}
		});

		button = new JButton("A*");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				aStar();

			}

		});



		button = new JButton("artPts");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (selectedNode == null) {
					
					List <Node> dcNodes = roadGraph.findAllArtpts();
					if (dcNodes.isEmpty()){
						System.out.println("Please select a node");
					}
					else{
						roadGraph.resetNodeDepth();
						artPtNodes = new ArrayList<Node>();
						ArtPtSearcher a;
						for (Node n : dcNodes){
							a = new ArtPtSearcher(n);
							for(Node node : a.articulationPoints){
								artPtNodes.add(node);
							}
						}	
						
					}
					drawing.repaint();
					//29260
					//node 7368
					//node 43364
					// 40430
					// 27828
					// 36837
					
				} else {
					roadGraph.resetNodeDepth();
					ArtPtSearcher a = new ArtPtSearcher(selectedNode);
					artPtNodes = a.articulationPoints;
					//System.out.println(artPtNodes.size());					
					drawing.repaint();
				}
			}
		});
		
		button = new JButton("clear");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				roadGraph.resetNodeDepth();
				roadGraph.resetNodesVisited();
				roadGraph.resetPathFrom();
				aStarSegs = null;
				artPtNodes = null;
				selectedSegments = null;
				strlengths = null;
				selectedNode = null;
				startNode = null;
				goalNode = null;		
				
				drawing.repaint();

			}

		});
		
		button = new JButton("A* time");
		panel.add(button);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				aStarTime();
			}
		});
		
		// Add a mouselistener to the drawing JComponent to respond to mouse
		// clicks.
		drawing.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				selectedNode = findNode(e.getPoint());

				if (selectedNode != null) {
					textOutput.setText(selectedNode.toString());
				}
				if (startNode == null) {
					startNode = selectedNode;
				}
				if (startNode != null) {
					goalNode = selectedNode;

				}
				drawing.repaint();
			}
		});

		// Once it is all set up, make the interface visible
		frame.setVisible(true);

	}
	/**
	 * uses astar search to find route from the start node to the goalnode
	 * constructs a map, fills the map with the roadname as the key and length
	 * as the value. then prints out all the roads and distance
	 */
	public void aStar(){
		if (startNode == null || goalNode == null) {
			textOutput
					.setText("please select a start intersection and an end intersection");
		} else {
			roadGraph.resetNodesVisited();
			roadGraph.resetPathFrom();
			strlengths = new LinkedHashMap<String, Double>();
			AStarSearcher s = new AStarSearcher(startNode, goalNode);
			s.aStarSearch();
			aStarSegs = s.getSegs();
			double length = 0;
			for (Segment seg : aStarSegs) {
				length = length + seg.getLength();
				String rdnm = seg.getRoad().getName();
				double len = seg.getLength();
				if (strlengths.containsKey(rdnm)){
					for (Map.Entry<String, Double> entry : strlengths.entrySet()){
						if (entry.getKey().equals(rdnm)){
							entry.setValue(entry.getValue()+len);
						}
					}
				}
				else{
					strlengths.put(rdnm, len);
				}



				//System.out.println(seg.getRoad().getName() + ": "+ seg.getLength() + "KM");
			}
			System.out.println();
			System.out.println("===============================================");
			System.out.println();
			for (Map.Entry<String, Double> entry : strlengths.entrySet()){
				
				System.out.printf("%s: %.3f %s \n",entry.getKey(),entry.getValue(), "KM");
			}
			
			System.out.printf("%s: %.3f %s \n", "Total Distance", length, "KM");
			//System.out.printf("total distance: " + length + "KM");
			System.out.println();
			System.out.println("===============================================");

			drawing.repaint();

			selectedNode = null;
			startNode = null;
			goalNode = null;
		}

	}
	
	/**
	 * uses astar search to find route from the start node to the goalnode
	 * constructs a map, fills the map with the roadname as a key and time
	 * as the value. then prints out all the roads and time taken
	 */
	private void aStarTime(){
		if (startNode == null || goalNode == null) {
			textOutput
					.setText("please select a start intersection and an end intersection");
		} else {
			roadGraph.resetNodesVisited();
			roadGraph.resetPathFrom();
			strlengths = new HashMap<String, Double>();
			AStarSearcher s = new AStarSearcher(startNode, goalNode);
			s.aStarTimeSearch();
			aStarSegs = s.getSegs();
			//seggysegs = s.seggys;
			double totalTime = 0;
			for (Segment seg : aStarSegs) {
				totalTime = totalTime + (seg.getLength()/seg.getRoad().getProperSpeed(seg.getRoad().getSpeed()));
				//System.out.println(seg.getRoad().getName() + ": " + (seg.getLength()/seg.getRoad().getProperSpeed(seg.getRoad().getSpeed())) * 60 + " mins");
				String rdnm = seg.getRoad().getName();
				double time = (seg.getLength()/seg.getRoad().getProperSpeed(seg.getRoad().getSpeed()));
				if (strlengths.containsKey(rdnm)){
					for (Map.Entry<String, Double> entry : strlengths.entrySet()){
						if (entry.getKey().equals(rdnm)){
							entry.setValue(entry.getValue()+time);
						}
					}
				}
				else{
					strlengths.put(rdnm, time);
				}
			}
			System.out.println();
			System.out.println("===============================================");
			System.out.println();
			for (Map.Entry<String, Double> entry : strlengths.entrySet()){				
				System.out.printf("%s: %.1f %s \n",entry.getKey(), (entry.getValue()*60), "Minutes");
			}
			System.out.println();
			System.out.printf("%s: %.1f %s \n", "Total Time", (totalTime*60), "Minutes");
			
			System.out.println("===============================================");

			drawing.repaint();

			selectedNode = null;
			startNode = null;
			goalNode = null;
		}
	}

	private double zoomFactor = 1.25;
	private double panFraction = 0.2;

	// set origin and scale for the whole map
	private void resetOrigin() {
		origin = new Location(westBoundary, northBoundary);
		scale = Math.min(windowSize / (eastBoundary - westBoundary), windowSize
				/ (northBoundary - southBoundary));
	}

	// shrink the scale (pixels/per km) by zoomFactor and move origin
	private void zoomOut() {
		scale = scale / zoomFactor;
		double deltaOrig = windowSize / scale * (zoomFactor - 1) / zoomFactor
				/ 2;
		origin = new Location(origin.x - deltaOrig, origin.y + deltaOrig);
		drawing.repaint();
	}

	// expand the scale (pixels/per km) by zoomFactor and move origin
	private void zoomIn() {
		double deltaOrig = windowSize / scale * (zoomFactor - 1) / zoomFactor
				/ 2;
		origin = new Location(origin.x + deltaOrig, origin.y - deltaOrig);
		scale = scale * zoomFactor;
		drawing.repaint();
	}

	private void pan(String dir) {
		double delta = windowSize * panFraction / scale;
		switch (dir) {
		case "left": {
			origin = new Location(origin.x - delta, origin.y);
			break;
		}
		case "right": {
			origin = new Location(origin.x + delta, origin.y);
			break;
		}
		case "up": {
			origin = new Location(origin.x, origin.y + delta);
			break;
		}
		case "down": {
			origin = new Location(origin.x, origin.y - delta);
			break;
		}
		}
		drawing.repaint();
	}

	// Find the place that the mouse was clicked on (if any)
	private Node findNode(Point mouse) {
		return roadGraph.findNode(mouse, origin, scale);
	}

	private void lookupName(String query) {
		List<String> names = new ArrayList(roadGraph.lookupName(query));
		if (names.isEmpty()) {
			selectedSegments = null;
			textOutput.setText("Not found");
		} else if (names.size() == 1) {
			String fullName = names.get(0);
			nameEntry.setText(fullName);
			textOutput.setText("Found");
			selectedSegments = roadGraph.getRoadSegments(fullName);
		} else {
			selectedSegments = null;
			String prefix = maxCommonPrefix(query, names);
			nameEntry.setText(prefix);
			textOutput.setText("Options: ");
			for (int i = 0; i < 10 && i < names.size(); i++) {
				textOutput.append(names.get(i));
				textOutput.append(", ");
			}
			if (names.size() > 10) {
				textOutput.append("...\n");
			} else {
				textOutput.append("\n");
			}
		}
	}

	private String maxCommonPrefix(String query, List<String> names) {
		String ans = query;
		for (int i = query.length();; i++) {
			if (names.get(0).length() < i)
				return ans;
			String cand = names.get(0).substring(0, i);
			for (String name : names) {
				if (name.length() < i)
					return ans;
				if (name.charAt(i - 1) != cand.charAt(i - 1))
					return ans;
			}
			ans = cand;
		}
	}

	// The redraw method that will be called from the drawing JComponent and
	// will
	// draw the map at the current scale and shift.
	public void redraw(Graphics g) {
		if (roadGraph != null && loaded) {
			roadGraph.redraw(g, origin, scale);
			if (selectedNode != null) {
				g.setColor(Color.red);
				selectedNode.draw(g, origin, scale);
			}

			if (startNode != null) {
				g.setColor(Color.GREEN);
				startNode.draw(g, origin, scale);
			}

			if (goalNode != null) {
				g.setColor(Color.red);
				goalNode.draw(g, origin, scale);
			}
		
			if (aStarSegs != null) {
				g.setColor(Color.red);
				for (Segment seg : aStarSegs) {
					seg.draw(g, origin, scale);
				}
			}
			if (selectedSegments != null) {
				g.setColor(Color.green);
				for (Segment seg : selectedSegments) {					
					seg.draw(g, origin, scale);
				}
			}
			if (artPtNodes != null) {
				g.setColor(Color.green);
				for (Node n : artPtNodes) {
					n.draw(g, origin, scale);
				}
			}
//			if (seggysegs != null) {
//				g.setColor(Color.pink);
//				for (Segment s : seggysegs){
//					s.draw(g, origin, scale);
//				}
//			}
		}
	}

	public static void main(String[] arguments) {
		if (arguments.length > 0) {
			AucklandMapper obj = new AucklandMapper(arguments[0]);
		} else {
			AucklandMapper obj = new AucklandMapper(null);
		}
	}

}
