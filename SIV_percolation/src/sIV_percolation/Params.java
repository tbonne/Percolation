package sIV_percolation;

import org.apache.commons.math3.distribution.NormalDistribution;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public class Params {
	
	//To do:
	//assumptions: birth stable for non infected pop, migration allowed to vary, generation of variation in population density in space.
	//Framework from sienna structures for the demographics model
	//develop visual representation of spread
	
	//Set assumpitons:
		//assuming asymetric network ties: individuals can be connected to someone and the individual not be connected back (could change)
		//assuming sex is 50/50: random between 0 and 1
		//assuming migration is independent of group size

	
	//List of parameters to set for each run
	final static Parameters p = RunEnvironment.getInstance().getParameters();

	//system
	public static int numThreads = 1;
	public static int endTime = 20000;
	
	//landscape
	public static int landscapeSize = 100;
	
	//Group nodes
	public static int numberOfGroups = 50;
	public static int maxRadiusOfConnections = 25; //beyond this no migration
	public static int groupSize = 50; //could make this a distribution

	//individual nodes
	public final static int maxAge = 15;  //(could make this more variable)
	public final static int individualEdges = 2; //(could make this more variable and systematic... centralized or not...)
	
	//Infections characteristics
	public static double latency = 5; 		//years (could make this a heavy tailed distribution
	public static double pTrans = 0.2; 		//% per year (could make this a function of delta Age + sex + time since infection

	public static double pMigration = 0.1; 	//% per year (distance based)
	public static double migrationAge = 4;
	

	//there seems to be a runtime issue: possibly an infinity loop
	
	//Constructor: used to set values from batch runs or the GUI
	public Params(){
			//randomSeed = (Integer)RandomHelper.nextIntFromTo(0, 1000000);
			numberOfGroups = (Integer)p.getValue("numberOfGroups");
			groupSize = (Integer)p.getValue("groupSize");
			landscapeSize = (Integer)p.getValue("landscapeSize");
			maxRadiusOfConnections = (Integer)p.getValue("maxRadiusOfConnections");
			pMigration = (Double)p.getValue("dispersalP");
			pTrans = (Double)p.getValue("pTrans");
		}
}
