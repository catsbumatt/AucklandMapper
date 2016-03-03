package Mapper;
import java.util.*;


public class ArtPtSearcher {


	public List <Node> articulationPoints = new ArrayList<Node>();
	private Stack <ArtPoint> artPts;
	private Node startNode;
	private int numSubTrees;

	public ArtPtSearcher(Node n){
		startNode = n;
		artPts = new Stack <ArtPoint>();
		startNode.depth = 0;
		numSubTrees = 0;
		for (Segment s : startNode.getOutNeighbours()){
			if (s.getEndNode().depth == Integer.MAX_VALUE){
				//recArtPts(s.getEndNode(), 1, startNode);
				
				findArtPoints(s.getEndNode(), startNode);
				numSubTrees++;
			}
		}
		if (numSubTrees > 1){
			articulationPoints.add(startNode);
		}

	}
	// Articulation points using a stack
	// done iteratively
	public void findArtPoints(Node firstNode, Node root){

		ArtPoint first = new ArtPoint(firstNode, 1, new ArtPoint (root, 0, null));
		artPts.push(first);
		while (!artPts.isEmpty()){

			ArtPoint elem = artPts.peek();
			Node node = elem.getNode();
			if (elem.getChildren() == null){
				
				node.depth = elem.getDepth();
				elem.setReachBack(elem.getDepth());
				elem.setChildren(new LinkedList<Node>());
				for (Segment s : node.getOutNeighbours()){
					if(!s.getEndNode().equals(elem.getParent().getNode())){
						
						elem.addChild(s.getEndNode());
					}
				}
			}
			else if (!elem.getChildren().isEmpty()){
				Node child = elem.getChildren().poll();
				if (child.depth < Integer.MAX_VALUE){
					elem.setReachBack(Math.min(elem.getReachBack(), child.depth));
				}
				else {
					ArtPoint c = new ArtPoint(child, node.depth + 1,elem);
					
					artPts.push(c);
				}
			}
			else{
				if (node != firstNode){

					if (elem.getReachBack() >= elem.getParent().getDepth()){
						articulationPoints.add(elem.getParent().getNode());
					}
					elem.getParent().setReachBack(Math.min(elem.getParent().getReachBack(), elem.getReachBack()));
					
				}
				
				artPts.pop();
			}
		}
	}


	public int recArtPts(Node node, int depth, Node fromNode){
		node.depth = depth;
		int reachBack = depth;
		for (Segment s : node.getOutNeighbours()){
			if (s.getEndNode() != fromNode){
				if (s.getEndNode().depth < Integer.MAX_VALUE){
					reachBack = Math.min(s.getEndNode().depth, reachBack);
				}
				else{
					int childReach = recArtPts(s.getEndNode(), depth + 1, node);
					reachBack = Math.min(childReach, reachBack);
					if (childReach >= depth){
						articulationPoints.add(node);
					}
				}
			}
		}
		return reachBack;
	}
}
