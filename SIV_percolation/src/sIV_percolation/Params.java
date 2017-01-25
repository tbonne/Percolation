package sIV_percolation;

import org.apache.commons.math3.distribution.NormalDistribution;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public class Params {
	
	//To do:
		//develop flow chart of the model + refine flow
		//clear up the two model analyses
	
	//Set assumpitons:
		//assuming non-directed network ties & individuals can form multiple connections to the same individual
		//assuming sex is 50/50: random between 0 and 1
		//assuming migration is independent of group size

	
	//List of parameters to set for each run
	final static Parameters p = RunEnvironment.getInstance().getParameters();

	
	//system
	public static int numThreads = 1;
	public static int endTime = 20000;
	public static int recordingDelta = 100;
	
	
	//landscape
	public static int landscapeSize = 100;
	
	
	//Group nodes
	public static int numberOfGroups = 50;
	public static int maxRadiusOfConnections = 25; //beyond this no migration
	public static int groupSize_optimal = 50; //could make this a distribution
	public static double avg_birthProb = 0.1; //this right now is manually built into the birth calculations in the group node step method

	
	//individual nodes
	public static NormalDistribution death_dist = new NormalDistribution(45,5);
	public static final int individualEdges = 2; //(could make this more variable and systematic... centralized or not...)
	public static double pMigration = 0.1; 	//% per year (distance based)
	public static double migrationAge = 5;
	public static final double prob_survival = 0.99; //probability of surviving each step (expected time of death = 1/(1-p). ex: 1/(1-0.96) = 25 )
	public static double beta_virulence = 0.1;
	public static double beta_age = 0.00;
	public static int reproStart = 5;
	public static int reproEnd = 15;
	public static double birthProb = 0.5;
	public static int interBirthPeriod = 2;
	
	
	//Infections characteristics
	public static NormalDistribution mutateProb = new NormalDistribution(0,0.1);		//rate of mutation of transmission parameters at each transmission event
	
	
	//start conditions
	public static final int maxAge_start = 25;  // Uniform random between 0 and this number
	public static final int initialInfectionSize = 50; //this is from one random group
	public static int groupSize_start = 50; //could make this a distribution
	public static double rate_start = 1.0;
	public static double shape_start = 8.0;
	public static double alpha_start = 7.0;


	//Constructor: used to set values from batch runs or the GUI
	public Params(){
			//randomSeed = (Integer)RandomHelper.nextIntFromTo(0, 1000000);
			numberOfGroups = (Integer)p.getValue("numberOfGroups");
			groupSize_optimal = (Integer)p.getValue("groupSize");
			landscapeSize = (Integer)p.getValue("landscapeSize");
			maxRadiusOfConnections = (Integer)p.getValue("maxRadiusOfConnections");
			pMigration = (Double)p.getValue("dispersalP");
			//pTrans = (Double)p.getValue("pTrans");
		}
}
