package sIV_percolation;

import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.RepastEdge;

public class GroupEdge extends RepastEdge{
	double weight;
	GroupNode start, end;
	double length=0;
	int infectionT;
	
	
	public GroupEdge( GroupNode s, GroupNode e,boolean directed,double weight){
		
		super(s, e, directed, weight);
		
		start = s;
		end = e;
		length=s.getCoord().distance(e.getCoord());
	}
	
	public void setWeight(double d){
		weight=d;
	}
	public GroupNode getStartNode(){
		return start;
	}
	public double getWeight(){
		return weight;
	}
	public GroupNode getEndNode(){
		return end;
	}
	public double getLength(){
		return length;
	}
	public int getInfectionT(){
		int ret = 0;
		if(start.getGroupStatus()==1)ret= 1;
		if(end.getGroupStatus()==1)ret= 1;
		infectionT=ret;
		
		return infectionT;
	}
}
