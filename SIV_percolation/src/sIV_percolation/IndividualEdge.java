package sIV_percolation;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.RepastEdge;

public class IndividualEdge extends RepastEdge{
	double weight;
	IndividualNode start, end;
	
	
	public IndividualEdge( IndividualNode s, IndividualNode e,boolean directed,double weight){
		
		super(s, e, directed, weight);
		
		start = s;
		end = e;
		this.weight=weight;
	}
	
	public void setWeight(double d){
		weight=d;
	}
	public IndividualNode getStartNode(){
		return start;
	}
	public double getWeight(){
		return weight;
	}
	public IndividualNode getEndNode(){
		return end;
	}
}
