package sIV_percolation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.graph.Network;

public class Observer {

	static ArrayList<Double> array_prop_ind_Infected;
	static ArrayList<Double> array_prop_group_Infected;
	static ArrayList<Double> array_sd_local_Infected;
	static ArrayList<Double> array_sd_global_Infected;
	static double[][] array_sd_global_STACF;
	static int count=0;




	public Observer(){
		array_prop_ind_Infected = new ArrayList<Double>();
		array_prop_group_Infected = new ArrayList<Double>();
		array_sd_local_Infected = new ArrayList<Double>();
		array_sd_global_Infected = new ArrayList<Double>();
		count=0;
		array_sd_global_STACF = new double[Params.recordingDelta][Params.numberOfGroups];
	}

	/*******************************
	 * during simulation recordings 
	 **************************/

	public static void step(){

		//get step
		double step = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		//do every step
		recordGroupInfections(count);
		count++;

		//do every x steps
		if(step%Params.recordingDelta==0){
			record_prop();
			record_sd();
			//record_STACF();
			count=0;
		}
	}

	private static void record_STACF(){
		
		int[][] asso = getAssociationMatrix();
		
		try {
			RConnection c = new RConnection();
			
			try {
				//Use R to estimate network measures
				REXP cosineX;
				//RList l;
				c.eval("library(igraph)");
				c.eval("library(starma)");
				
				//create graph
				assignAsRMatrix(c,asso,"asso");
				c.eval("g.1=graph.adjacency(asso,mode='undirected',weighted=NULL) ");
				c.eval("g.i=diag()");

				//caculate in R the distance D from the observed distributions
				//c.assign("offP", asso);
				//c.assign("motherP", motherArray);
				c.eval("cosSim <- cosine(offP,motherP)");
				cosineX = c.eval("cosSim");
				double cos = cosineX.asDouble();
				

			} catch (REngineException re){
				re.printStackTrace();
				System.out.println("Failed to estimate cosine similarity: Rengine");
			} catch (REXPMismatchException rm){
				rm.printStackTrace();
				System.out.println("Failed to estimate cosine similarity: REXP Mismatch");

			}
			
			c.close();
		} catch (RserveException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
    public static REXP assignAsRMatrix(RConnection c, int[][] sourceArray, String nameToAssignOn) throws REngineException {
        if (sourceArray.length == 0) {
            return null;
        }

        c.assign(nameToAssignOn, sourceArray[0]);
        REXP resultMatrix = c.eval(nameToAssignOn + " <- matrix( " + nameToAssignOn + " ,nr=1)");
        for (int i = 1; i < sourceArray.length; i++) {
            c.assign("temp", sourceArray[i]);
            resultMatrix = c.eval(nameToAssignOn + " <- rbind(" + nameToAssignOn + ",matrix(temp,nr=1))");
        }

        return resultMatrix;
    }

	private static int[][] getAssociationMatrix(){

		ArrayList<GroupNode> nodes = ModelSetup.getAllGroups();
		Network net = ModelSetup.getGroupNet();
		int[][] associationM = new int[nodes.size()][nodes.size()];

		for(int i =0; i<nodes.size(); i++){
			for(int j = 0;j<nodes.size();j++){
				if(net.isAdjacent(nodes.get(i), nodes.get(j))){
					associationM[i][j]=1;
				} else {
					associationM[i][j]=0;
				}
			}
		}
		return associationM;
	}

	private static void recordGroupInfections(int i){
		for(GroupNode gn : ModelSetup.getAllGroups()){
			array_sd_global_STACF[i][gn.id] = gn.getGroupStatus();
		}
	}

	private static void record_prop(){

		//total proportion infected: individuals
		int count_ind = 0;
		for(IndividualNode n : ModelSetup.getAllIndividuals()){
			if(n.getStatus()==1)count_ind++;
		}
		array_prop_ind_Infected.add( (double)count_ind/ (double)(ModelSetup.getAllIndividuals().size()));


		//total proportion infected: groups
		int count_group = 0;
		for(GroupNode n : ModelSetup.getAllGroups()){
			if(n.getGroupStatus()>0)count_group++;
		}
		array_prop_group_Infected.add( (double)count_group/ (double)(ModelSetup.getAllGroups().size()));
	}

	private static void record_sd(){

		//local sd (sum of squares differences in proportion infected between connected groups)
		ArrayList<GroupNode> alreadyCalculated = new ArrayList<GroupNode>();
		double localSS=0;

		//for each group node
		for(GroupNode focalN: ModelSetup.getAllGroups()){

			if(alreadyCalculated.contains(focalN)==false){

				//calculate focal troops proportion infected
				double focalN_prop = (double)(focalN.getGroupStatus())/(double)(focalN.getGroupSize());

				//iterate over connected neighbours
				Iterable<GroupNode> neighs = ModelSetup.getGroupNet().getAdjacent(focalN);
				for(GroupNode neighNode: neighs){
					if(neighNode.getGroupSize()>0 && alreadyCalculated.contains(neighNode)==false){
						double neighN_prop = (double)(neighNode.getGroupStatus())/(double)(neighNode.getGroupSize());
						localSS = localSS + Math.pow((focalN_prop - neighN_prop),2);
					}
				}

				//keep track of who is already recorded
				alreadyCalculated.add(focalN);
			}

		}

		array_sd_local_Infected.add(localSS);


		//global sd (sum of squares difference in proportion infected between all groups
		ArrayList<GroupNode> alreadyCalculated_global = new ArrayList<GroupNode>();
		double globalSS=0;

		//for each group node
		for(GroupNode focalN: ModelSetup.getAllGroups()){

			if(alreadyCalculated_global.contains(focalN)==false){

				//calculate focal troops proportion infected
				double focalN_prop = (double)(focalN.getGroupStatus())/(double)(focalN.getGroupSize());

				//iterate over all groups
				for(GroupNode neighNode: ModelSetup.getAllGroups()){
					if(neighNode.getGroupSize()>0 && alreadyCalculated_global.contains(neighNode)==false){
						double neighN_prop = (double)(neighNode.getGroupStatus())/(double)(neighNode.getGroupSize());
						globalSS = globalSS + Math.pow((focalN_prop - neighN_prop),2);
					}
				}

				//keep track of who is already recorded
				alreadyCalculated_global.add(focalN);
			}

		}

		array_sd_global_Infected.add(globalSS);


	}


	/*******************************
	 * Output recordings to csv at the end of the simulation
	 **************************/

	public static void output(){

		//Create the writer and the output file
		BufferedWriter summaryStats_out=null;
		try {
			summaryStats_out = new BufferedWriter(new FileWriter("Infection_output.csv", false));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Transfer the recorded data to the output file: measures
		try {

			//record parameters
			summaryStats_out.append("latencyStart = ");
			summaryStats_out.append(((Double)Params.rate_start).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("MigrationAge = ");
			summaryStats_out.append(((Double)Params.migrationAge).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("pMigration = ");
			summaryStats_out.append(((Double)Params.pMigration).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("endTime = ");
			summaryStats_out.append(((Integer)Params.endTime).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("OptimalGrouSize = ");
			summaryStats_out.append(((Integer)Params.groupSize_optimal).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("ProbSurvival = ");
			summaryStats_out.append(((Double)Params.prob_survival).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("RadiusConnections = ");
			summaryStats_out.append(((Integer)Params.maxRadiusOfConnections).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("numGroups = ");
			summaryStats_out.append(((Integer)Params.numberOfGroups).toString());
			summaryStats_out.append(", ");
			summaryStats_out.newLine();
			summaryStats_out.newLine();

			//set header
			summaryStats_out.append("Prop_ind,Prop_group,SS_global,SS_local,TimeStamp");
			summaryStats_out.newLine();

			//record values
			for(int i = 0 ; i<array_sd_global_Infected.size();i++){

				summaryStats_out.append(((Double)array_prop_ind_Infected.get(i)).toString());
				summaryStats_out.append(",");
				summaryStats_out.append(((Double)array_prop_group_Infected.get(i)).toString());
				summaryStats_out.append(",");
				summaryStats_out.append(((Double)array_sd_global_Infected.get(i)).toString());
				summaryStats_out.append(",");
				summaryStats_out.append(((Double)array_sd_local_Infected.get(i)).toString());
				summaryStats_out.append(",");
				summaryStats_out.append(((Integer)(i)).toString());
				summaryStats_out.newLine();
			}

			summaryStats_out.flush();
			summaryStats_out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}



}
