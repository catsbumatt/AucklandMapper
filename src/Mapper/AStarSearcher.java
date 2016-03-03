package Mapper;
import java.util.*;

public class AStarSearcher {

	public Node startNode;
	public Node goalNode;

	
	private Queue <AStarNode> fringe;// = new PriorityQueue <Node>();
	public double estimate;
	private List <Segment> segs = new ArrayList<Segment>();
	public List <Segment> seggys = new ArrayList<Segment>();
	//private List <Node> finalNodes = new ArrayList<Node>();



	private Comparator astarComp = new AStarComparator();

	public AStarSearcher(Node start, Node goal){
		startNode = start;
		goalNode = goal;
	}
	
	/**
	 * basic a star search algorithm given in lectures
	 */
	public void aStarSearch(){
		// need to change all nodes visited to false
		// need to change all nodes pathfrom to null		
		fringe = new PriorityQueue<AStarNode>(1, astarComp);
		AStarNode a = new AStarNode(startNode, null, 0 ,estimate(startNode, goalNode), null);
		fringe.offer(a);
		while (!fringe.isEmpty()){
			AStarNode n = fringe.poll();
			if (!n.getNode().visited){
				n.getNode().visited = true;
				//n.getNode().pathFrom = n.getFromNode();
				n.getNode().cost = n.getCostToHere();
				if (n.getNode() == goalNode){
					//EXIT
					fillSegs(n);
					return;
				}
				for (Segment s : n.getNode().outNeighbours){
					if (!s.getEndNode().visited){
						//segs.add(s);
						double costToNeighbour = n.getCostToHere() + s.getLength();
						Node neighbour = s.getEndNode();
						double estTotal = costToNeighbour + estimate(neighbour, goalNode);
						AStarNode b = new AStarNode(neighbour, n.getNode(), costToNeighbour, estTotal, n);
						fringe.offer(b);
						//segs.add(fringe.peek().getSegment());
					}
				}
			}

		}

	}
	
	/**
	 * basic a star search algorithm, this time the hueristic has changed to
	 * time, not length.
	 */
	public void aStarTimeSearch(){
		// need to change all nodes visited to false
		// need to change all nodes pathfrom to null		
		fringe = new PriorityQueue<AStarNode>(1, astarComp);
		AStarNode a = new AStarNode(startNode, null, 0 ,estimate(startNode, goalNode), null);
		fringe.offer(a);
		while (!fringe.isEmpty()){
			AStarNode n = fringe.poll();
			if (!n.getNode().visited){
				n.getNode().visited = true;				
				n.getNode().cost = n.getCostToHere();
				if (n.getNode() == goalNode){
					//EXIT
					fillSegs(n);
					return;
				}
				for (Segment s : n.getNode().outNeighbours){
					if (!s.getEndNode().visited){	
						seggys.add(s);
						double costToNeighbour = n.getCostToHere() + (s.getLength()/s.getRoad().getProperSpeed(s.getRoad().getSpeed()));
						Node neighbour = s.getEndNode();
						double estTotal = costToNeighbour + estimate(neighbour, goalNode);
						AStarNode b = new AStarNode(neighbour, n.getNode(), costToNeighbour, estTotal, n);
						fringe.offer(b);
						//segs.add(fringe.peek().getSegment());
					}
				}
			}

		}

	}
	
	/**
	 * constructs a list from goalnode to startnode given one aStarNode
	 * that astarnode has a link all the way back to the starting astarnode
	 * the listof segments is constructed in reverse order though
	 * @param a the goal astarnode
	 */
	public void fillSegs(AStarNode a){
		AStarNode last = a;
		while (last.getFromNode() != null){
			//finalNodes.add(last.getNode());
			//System.out.println(last.getNode());
			for (Segment s : last.getNode().getInNeighbours()){
				if (s.getStartNode() == last.getParent().getNode() && s.getEndNode() == last.getNode()){
					segs.add(s);					
				}
			}
			last = last.getParent();
		}
	}
	
	/**
	 * reverses the order of the segments
	 * @return reversed list of segments
	 */
	public List<Segment> getSegs (){
		List <Segment> segments = new ArrayList<Segment>();
		for (int i = segs.size()-1; i>=0; i--){
			segments.add(segs.get(i));
		}
		return segments;


	}

	/**
	 * used as a hueristic for a star search
	 * @param start the from node
	 * @param goal th goal node
	 * @return the distance between both nodes
	 */
	public double estimate(Node start, Node goal){
		 return start.getLoc().distanceTo(goal.getLoc());
	}


	



	private static class AStarComparator implements Comparator<AStarNode> {

		@Override
		public int compare(AStarNode o1, AStarNode o2) {

			return (int)(Math.signum(o1.getEstimate() - o2.getEstimate()));
		}

	}


}
