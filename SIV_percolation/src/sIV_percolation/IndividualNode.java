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
import repast.simphony.util.ContextUtils;

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

	public IndividualNode(Network<IndividualNode> iNet, GroupNode group, boolean born) {
		age=0;
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
			socialMech();
			infectionStep();
			birth();
			migrationStep();
		} else{
			ModelSetup.getIndividualsToRemove().add(this);
		}
	}


	/*****************************************methods**************************************************/

	//The behaviour that determines possible transmission routes between individuals within a group
	private void socialMech(){
		SAN_randomXTies(Params.individualEdges);
		//SAN_outDegree_sex(Params.individualEdges);
		//SAN_outDegree_sex_age(Params.individualEdges);
		//SAN_outDegree_sex_age_inDegree(Params.individualEdges);
	}

	private void infectionStep(){

		//step infection time
		if(this.iStatus==1)PIV.stepT();
		//if(this.iStatus==0)infect(Params.rate_start,Params.shape_start,Params.alpha_start);
		
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
				ModelSetup.getIndividualsToAdd().add(new IndividualNode(this.myNet,this.myGroup, true));
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
		ModelSetup.getContext().add(PIV);
		ModelSetup.getInfectionSpace().moveTo(PIV,100*newR,100*newS);

	}
	
	/*****************************************Stochastic actor based models of tie formation**************************************************/
	
	//simple random model starting from sexual maturity
	private void SAN_randomXTies(int ties){
		
		ModelSetup.removeEdges(this);
		
		if (this.age>Params.reproStart){
			int count=0;
			for(IndividualNode nn: this.getMyGroup().myIndividuals){
				if(nn!=this && nn.getAge()>Params.reproStart){
					IndividualEdge edge = new IndividualEdge(this,nn,false,1);
					ContextUtils.getContext(this).add(edge);
					myNet.addEdge(edge);
					count++;
				}
				if(count>=ties)break;
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
	public int getAge(){
		return age;
	}
	public PIV_infection getPIV(){
		return PIV;
	}
	


}
