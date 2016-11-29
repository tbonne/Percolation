package sIV_percolation;

import java.util.ArrayList;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.java.plugin.Plugin;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;

import com.sun.media.sound.ModelDestination;
import com.vividsolutions.jts.geom.Coordinate;

public class GroupNode {  

	ArrayList<IndividualNode> myIndividuals;
	int id,size,status;
	Coordinate coord;
	private ContinuousSpace <Object > groupSpace; 
	private Network<GroupNode> myNet;
	double probBirth;


	public GroupNode(ContinuousSpace <Object > space,Network <GroupNode> net,int i, Coordinate coordinate) {
		myIndividuals = new ArrayList<IndividualNode>();
		id = i;
		coord = coordinate;
		this.groupSpace=space;
		this.myNet=net;
		probBirth=0;
	}

	public void step(){ 

		status=0;
		
		for(IndividualNode nn : myIndividuals){
			if(nn.getStatus()==1)status++;
		}
		size=myIndividuals.size();
		
		
		//probBirth = 1/(1+9*Math.exp(-1*(Params.groupSize_optimal-this.size)*0.1)); //at optimal group size the birth rate is 0.1
		probBirth = 1/(1+Math.exp(-1*(Params.groupSize_optimal-this.size)/10)); 
	}


	/*****************************************methods**************************************************/




	/*****************************************get/set methods**************************************************/
	
	public double getProbBirth(){
		return probBirth;
	}
	public int getID(){
		return id;
	}
	public Coordinate getCoord(){
		return coord;
	}
	public ArrayList<IndividualNode> getIndividualNodes(){
		return myIndividuals;
	}
	public void setGroupSize(int i){
		size = i;
	}
	public int getGroupStatus(){
		return status;
	}
	public int getGroupSize(){
		return size;
	}
	public double getPrev(){
		return (double)status/(double)size;
	}
}
