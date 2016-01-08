package sIV_percolation;

import java.util.ArrayList;
import java.util.Iterator;

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
import repast.simphony.space.graph.RepastEdge;

import com.sun.media.sound.ModelDestination;
import com.vividsolutions.jts.geom.Coordinate;

public class IndividualNode {  

	GroupNode myGroup;
	int age,iStatus,sex;
	private ContinuousSpace <Object > mySpace; 
	private Network<IndividualNode> myNet;


	public IndividualNode(ContinuousSpace <Object > space,Network<IndividualNode> iNet, GroupNode group) {
		age=RandomHelper.nextIntFromTo(0, Params.maxAge);
		sex = RandomHelper.nextIntFromTo(0, 1);
		this.mySpace=space;
		this.myNet = iNet;
		myGroup=group;
		iStatus=0;
	}


	public void step(){

		boolean alive=birthDeath();
		if (alive){
			migrationStep();
		}
	}


	/*****************************************methods**************************************************/


	private boolean birthDeath(){

		boolean retval=true;
		//aging
		age++;

		if(age>Params.maxAge){

			retval=false;
			//remove this individual at the end of the step
			ModelSetup.getIndividualsToRemove().add(this);

			//add new individual to the group
			ModelSetup.getIndividualsToAdd().add(new IndividualNode(this.mySpace,this.myNet,this.myGroup));

		}
		return retval;
	}

	private void migrationStep(){

		if(age==Params.migrationAge){
			//double chance = Params.pMigration + (Params.pMigration/10)*(this.myGroup.myIndividuals.size()-Params.groupSize);
			double chance = Params.pMigration;
			//System.out.println("probM = " +chance+"    groupSize = "+this.myGroup.myIndividuals.size());
			if(RandomHelper.nextDouble()<=chance){
				ModelSetup.addMigratingInd(this);
			}
		}
	}

	/*****************************************get/set methods**************************************************/

	public int getStatus(){
		return iStatus;
	}
	public GroupNode getMyGroup(){
		return myGroup;
	}
	public void setMyGroup(GroupNode myG){
		myGroup = myG;
	}

}
