package mapper;
import java.util.*;

public class Searcher {

	public Node startNode;
	public Node goalNode;

	private List<Node> visitedNodes; // = new ArrayList<Node>();
	private Queue <AStarNode> fringe;// = new PriorityQueue <Node>();
	public double estimate;
	private List <Segment> segs = new ArrayList<Segment>();

	private Comparator astarComp = new AStarComparator();

	public Searcher(Node start, Node goal){
		startNode = start;
		goalNode = goal;
	}

	public void aStarSearch(){
		// need to change all nodes visited to false
		// need to change all nodes pathfrom to null

		visitedNodes = new ArrayList<Node>();
		fringe = new PriorityQueue<AStarNode>(1, astarComp);
		AStarNode a = new AStarNode(startNode, null, 0 ,estimate(startNode, goalNode));
		fringe.offer(a);
		while (!fringe.isEmpty()){
			AStarNode n = fringe.poll();
			if (!n.getNode().visited){
				n.getNode().visited = true;
				n.getNode().pathFrom = n.getFromNode();
				n.getNode().cost = n.getCostToHere();
				if (n.getNode() == goalNode){
					//EXIT
					return;
				}
				for (Segment s : n.getNode().outNeighbours){
					if (!s.getEndNode().visited){
						segs.add(s);
						double costToNeighbour = n.getCostToHere() + s.getLength();
						Node neighbour = s.getEndNode();
						double estTotal = costToNeighbour + estimate(neighbour, goalNode);
						AStarNode b = new AStarNode(neighbour, n.getNode(), costToNeighbour, estTotal);
						fringe.offer(b);
						//segs.add(fringe.peek().getSegment());
					}
				}
			}

		}
	}
	
	public List getSegs (){
		return segs;
			
		
	}


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
