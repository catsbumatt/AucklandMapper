package Mapper;

import java.util.*;

public class AStarNode implements Comparable<AStarNode> {
	private Node node;
	private Node fromNode;
	private double costToHere;
	private double estimate;
	private AStarNode parent;
	
	//private Segment segment;


	public AStarNode(Node start, Node from, double costToHere, double est, AStarNode parent){
		node = start;
		fromNode = from;
		this.setCostToHere(costToHere);
		estimate = est;
		this.setParent(parent);
		//setSegment(seg);
	}


	public double getCostToHere() {
		return costToHere;
	}


	public Node getNode() {
		return node;
	}


	public void setNode(Node node) {
		this.node = node;
	}


	public Node getFromNode() {
		return fromNode;
	}


	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}


	public double getEstimate() {
		return estimate;
	}


	public void setEstimate(double estimate) {
		this.estimate = estimate;
	}


	public void setCostToHere(double costToHere) {
		this.costToHere = costToHere;
	}


	public int compareTo(AStarNode o) {
		return (int)(Math.signum(this.getEstimate() - o.getEstimate()));
	}


	public AStarNode getParent() {
		return parent;
	}


	public void setParent(AStarNode parent) {
		this.parent = parent;
	}

	
}
