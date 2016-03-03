package Mapper;
import java.util.*;


public class ArtPoint {
	private Node node;
	private Node root;
	private int depth;
	private int reachBack;
	private ArtPoint parent;
	private Queue<Node> children = null;




	// = new ArrayList<Node>()
	public ArtPoint (Node first, int depth, ArtPoint parent){
		setNode(first);
		this.setDepth(depth);
		this.setParent(parent);
	}




	public Queue<Node> getChildren() {
		return children;
	}




	public void setChildren(Queue<Node> children) {
		this.children = children;
	}

	public void addChild(Node n){
		children.offer(n);
	}




	public int getReachBack() {
		return reachBack;
	}




	public void setReachBack(int reachBack) {
		this.reachBack = reachBack;
	}




	public int getDepth() {
		return depth;
	}




	public void setDepth(int depth) {
		this.depth = depth;
	}




	public Node getRoot() {
		return root;
	}




	public void setRoot(Node root) {
		this.root = root;
	}




	public Node getNode() {
		return node;
	}




	public void setNode(Node firstNode) {
		this.node = firstNode;
	}




	public ArtPoint getParent() {
		return parent;
	}




	public void setParent(ArtPoint parent) {
		this.parent = parent;
	}


}
