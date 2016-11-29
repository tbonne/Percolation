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
	int age,iStatus,sex,maxAge;
	private Network<IndividualNode> myNet;
	PIV_infection PIV;
	private int birthCount;


	public IndividualNode(Network<IndividualNode> iNet, GroupNode group) {
		age=RandomHelper.nextIntFromTo(0, Params.maxAge_start);
		sex = RandomHelper.nextIntFromTo(0, 1);
		//this.mySpace=space;
		this.myNet = iNet;
		myGroup=group;
		iStatus=0;
		maxAge = (int)Params.death_dist.sample();
		birthCount=Params.interBirthPeriod;
		PIV=null;
	}


	public void step(){
		
		if(PIV!=null)this.iStatus=1;

		//aging
		age++;
		if (age<maxAge){
			infectionStep();
			birth();
			migrationStep();
		} else{
			ModelSetup.getIndividualsToRemove().add(this);
		}
	}


	/*****************************************methods**************************************************/


	private void infectionStep(){

		//step infection time
		if(this.iStatus==1)PIV.stepT();
		
		//determine death from infection
		if(this.getStatus()==1){
			double prob_death=(Params.beta_virulence)*this.PIV.getTransProb();
			if(RandomHelper.nextDouble()<prob_death ){
				ModelSetup.getIndividualsToRemove().add(this);
			}
		}
	}

	private void birth(){

		if(age>=Params.reproStart && age <= Params.reproEnd){
			if(this.birthCount>=Params.interBirthPeriod){
			double prob_birth = this.myGroup.getProbBirth();
			double ran = RandomHelper.nextDouble();
			if(ran<prob_birth){
				//add new individual to the group
				ModelSetup.getIndividualsToAdd().add(new IndividualNode(this.myNet,this.myGroup));
				birthCount=0;
			}
			}else{
				birthCount++;
			}
		}
		
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

	public void infect(double r,double s,double a){

		//		double newL = l + Params.mutateProb.sample();
		//		double newW = w + Params.mutateProb.sample();
		//		double newC = c + Params.mutateProb.sample();
		//		double newS = s + Params.mutateProb.sample();
		double newA = a + Params.mutateProb.sample();
		double newR = r + Params.mutateProb.sample();
		double newS = s + Params.mutateProb.sample();

		//		if(newL<0)newL=0;
		//		if(newS>newW)newS=newW;
		//		if(newS<0.01)newS=0.01;
		//		if(newC>newW)newC=newW;
		//		if(newC<0)newC=0;
		if(newA<=0)newA=0.001;
		if(newR<=0)newR=0.001;
		if(newS<=0)newS=0.001;

		PIV = new PIV_infection(newR,newS,newA);

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
