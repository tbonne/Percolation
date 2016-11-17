package sIV_percolation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import repast.simphony.engine.environment.RunEnvironment;

public class Observer {

	static ArrayList<Double> array_prop_ind_Infected;
	static ArrayList<Double> array_prop_group_Infected;
	static ArrayList<Double> array_sd_local_Infected;
	static ArrayList<Double> array_sd_global_Infected;


	public Observer(){
		array_prop_ind_Infected = new ArrayList<Double>();
		array_prop_group_Infected = new ArrayList<Double>();
		array_sd_local_Infected = new ArrayList<Double>();
		array_sd_global_Infected = new ArrayList<Double>();
	}

	/*******************************
	 * during simulation recordings 
	 **************************/

	public static void step(){

		//do every step
		//noting right now

		//do every x steps
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()%10==0){
			record_prop();
			record_sd();	
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

		//Transfer the recorded data to the output file
		try {

			//record parameters
			summaryStats_out.append("latency = ");
			summaryStats_out.append(((Double)Params.latency).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("MigrationAge = ");
			summaryStats_out.append(((Double)Params.migrationAge).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("pMigration = ");
			summaryStats_out.append(((Double)Params.pMigration).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("pTrans = ");
			summaryStats_out.append(((Double)Params.pTrans).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("endTime = ");
			summaryStats_out.append(((Integer)Params.endTime).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("grouSize = ");
			summaryStats_out.append(((Integer)Params.groupSize).toString());
			summaryStats_out.append(", ");
			summaryStats_out.append("maxAge = ");
			summaryStats_out.append(((Integer)Params.maxAge).toString());
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
