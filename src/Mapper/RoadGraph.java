package Mapper;

/* Code for COMP261 Assignment
 * Name:
 * Usercode:
 * ID:

 */

import java.util.*;
import java.io.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;

/** RoadMap: The list of the roads and the graph of the road network */

public class RoadGraph {

	double westBoundary = Double.POSITIVE_INFINITY;
	double eastBoundary = Double.NEGATIVE_INFINITY;
	double southBoundary = Double.POSITIVE_INFINITY;
	double northBoundary = Double.NEGATIVE_INFINITY;

	// the map containing the graph of nodes (and roadsegments), indexed by the
	// nodeID
	Map<Integer, Node> nodes = new HashMap<Integer, Node>();

	// the map of roads, indexed by the roadID
	Map<Integer, Road> roads = new HashMap<Integer, Road>();;

	// the map of roads, indexed by name
	Map<String, Set<Road>> roadsByName = new HashMap<String, Set<Road>>();;

	Set<String> roadNames = new HashSet<String>();

	/** Construct a new RoadMap object */
	public RoadGraph() {
	}

	public String loadData(String dataDirectory) {
		// Read roads into roads array.
		// Read the nodes into the roadGraph array.
		// Read each road segment
		// put the segment into the neighbours of the startNode
		// If the road of the segment is not one way,
		// also construct the reversed segment and put it into
		// the neighbours of the endNode
		// Work out the boundaries of the region.
		String report = "";
		System.out.println("Loading roads...");
		loadRoads(dataDirectory);
		report += String.format(
				"Loaded %,d roads, with %,d distinct road names%n", roads
						.entrySet().size(), roadNames.size());
		System.out.println("Loading intersections...");
		loadNodes(dataDirectory);
		report += String.format("Loaded %,d intersections%n", nodes.entrySet()
				.size());
		System.out.println("Loading road segments...");
		loadSegments(dataDirectory);
		report += String.format("Loaded %,d road segments%n", numSegments());
		return report;
	}

	public void loadRoads(String dataDirectory) {
		File roadFile = new File(dataDirectory + "roadID-roadInfo.tab");
		if (!roadFile.exists()) {
			System.out.println("roadID-roadInfo.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(roadFile));
			data.readLine(); // throw away header line.
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Road road = new Road(line);
				roads.put(road.getID(), road);
				String fullName = road.getFullName();
				roadNames.add(fullName);
				Set<Road> rds = roadsByName.get(fullName);
				if (rds == null) {
					rds = new HashSet<Road>(4);
					roadsByName.put(fullName, rds);
				}
				rds.add(road);
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public void loadNodes(String dataDirectory) {
		File nodeFile = new File(dataDirectory + "nodeID-lat-lon.tab");
		if (!nodeFile.exists()) {
			System.out.println("nodeID-lat-lon.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(nodeFile));
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Node node = new Node(line);
				nodes.put(node.getID(), node);
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public void loadSegments(String dataDirectory) {
		File segFile = new File(dataDirectory
				+ "roadSeg-roadID-length-nodeID-nodeID-coords.tab");
		if (!segFile.exists()) {
			System.out
					.println("roadSeg-roadID-length-nodeID-nodeID-coords.tab not found");
			return;
		}
		BufferedReader data;
		try {
			data = new BufferedReader(new FileReader(segFile));
			data.readLine(); // get rid of headers
			while (true) {
				String line = data.readLine();
				if (line == null) {
					break;
				}
				Segment seg = new Segment(line, roads, nodes);
				// System.out.println(seg);
				Node node1 = seg.getStartNode();
				Node node2 = seg.getEndNode();
				node1.addOutSegment(seg);
				node2.addInSegment(seg);
				Road road = seg.getRoad();
				road.addSegment(seg);
				if (!road.isOneWay()) {
					Segment revSeg = seg.reverse();
					node2.addOutSegment(revSeg);
					node1.addInSegment(revSeg);
				}
			}
		} catch (IOException e) {
			System.out.println("Failed to open roadID-roadInfo.tab: " + e);
		}
	}

	public void loadRestrictions(String dataDirectory){
		File resFile = new File(dataDirectory + "restrictions.tab");
		if (!resFile.exists()){
			System.out.println("NO RESTRICTION FILE.");
			return;
		}
		BufferedReader data;
		try{
			data = new BufferedReader(new FileReader(resFile));
			data.readLine();
			while(true){
				String line = data.readLine();
				if (line  == null){
					break;
				}

			}
		}catch (IOException e) {
			System.out.println("Failed to open restrictions.tab: " + e);
		}
	}

	public double[] getBoundaries() {
		double west = Double.POSITIVE_INFINITY;
		double east = Double.NEGATIVE_INFINITY;
		double south = Double.POSITIVE_INFINITY;
		double north = Double.NEGATIVE_INFINITY;

		for (Node node : nodes.values()) {
			Location loc = node.getLoc();
			if (loc.x < west) {
				west = loc.x;
			}
			if (loc.x > east) {
				east = loc.x;
			}
			if (loc.y < south) {
				south = loc.y;
			}
			if (loc.y > north) {
				north = loc.y;
			}
		}
		return new double[] { west, east, south, north };
	}

	public void checkNodes() {
		for (Node node : nodes.values()) {
			if (node.getOutNeighbours().isEmpty()
					&& node.getInNeighbours().isEmpty()) {
				System.out.println("Orphan: " + node);
			}
		}
	}

	public int numSegments() {
		int ans = 0;
		for (Node node : nodes.values()) {
			ans += node.getOutNeighbours().size();
		}
		return ans;
	}

	public void redraw(Graphics g, Location origin, double scale) {
		// System.out.printf("Drawing road graph. at (%.2f, %.2f) @ %.3f%n",
		// origX, origY, scale);
		g.setColor(Color.black);
		for (Node node : nodes.values()) {
			for (Segment seg : node.getOutNeighbours()) {
				seg.draw(g, origin, scale);
			}
		}
		g.setColor(Color.blue);
		for (Node node : nodes.values()) {
			node.draw(g, origin, scale);
		}
	}

	private double mouseThreshold = 5; // how close does the mouse have to be?

	public Node findNode(Point point, Location origin, double scale) {
		Location mousePlace = Location.newFromPoint(point, origin, scale);
		/*
		 * System.out.printf("find at %d %d -> %.3f %.3f -> %d %d %n", point.x,
		 * point.y, x, y, (int)((x-origX)*scale),(int)((y-origY)*(-scale)) );
		 */
		Node closestNode = null;
		double mindist = Double.POSITIVE_INFINITY;
		for (Node node : nodes.values()) {
			double dist = node.distanceTo(mousePlace);
			if (dist < mindist) {
				mindist = dist;
				closestNode = node;
			}
		}
		return closestNode;
	}

	/**
	 * Returns a set of full road names that match the query. If the query
	 * matches a full road name exactly, then it returns just that name
	 */
	public Set<String> lookupName(String query) {
		Set<String> ans = new HashSet<String>(10);
		if (query == null)
			return null;
		query = query.toLowerCase();
		for (String name : roadNames) {
			if (name.equals(query)) { // this is the right answer
				ans.clear();
				ans.add(name);
				return ans;
			}
			if (name.startsWith(query)) { // it is an option
				ans.add(name);
			}
		}
		return ans;
	}

	/** Get the Road objects associated with a (full) road name */
	public Set<Road> getRoadsByName(String fullname) {
		return roadsByName.get(fullname);
	}

	/**
	 * Return a list of all the segments belonging to the road with the given
	 * (full) name.
	 */
	public List<Segment> getRoadSegments(String fullname) {
		Set<Road> rds = roadsByName.get(fullname);
		if (rds == null) {
			return null;
		}
		System.out.println("Found " + rds.size() + " road objects: "
				+ rds.iterator().next());
		List<Segment> ans = new ArrayList<Segment>();
		for (Road road : rds) {
			ans.addAll(road.getSegments());
		}
		return ans;
	}

	public Map<Integer, Node> getNodes() {
		return nodes;
	}

	public void resetNodesVisited() {
		for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
			entry.getValue().visited = false;
		}
	}

	public void resetPathFrom() {
		for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
			entry.getValue().pathFrom = null;
		}
	}
	public void resetNodeDepth(){
		for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
			entry.getValue().depth = Integer.MAX_VALUE;
		}
	}
	
	public List<Node> findAllArtpts(){
		List<Node> ns = new ArrayList<Node>();
		
		for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
			if(entry.getValue().getID() == 15007){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 27851){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 27981){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 6792){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 43381){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 36453){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 27769){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 20634){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 20206){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 40551){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 29347){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 34642){
				ns.add(entry.getValue());
			}
			if(entry.getValue().getID() == 33961){
				ns.add(entry.getValue());
			}
			
			
		}
		return ns;
	}

	public static void main(String[] arguments) {
		AucklandMapper.main(arguments);
	}

}
