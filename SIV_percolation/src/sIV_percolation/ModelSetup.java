package sIV_percolation;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.linear.ArrayRealVector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameter;
import repast.simphony.query.space.continuous.ContinuousWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;

public class ModelSetup implements ContextBuilder<Object>{

	private static Context mainContext;
	public static ArrayList<GroupNode> allGroups;
	public static ArrayList<IndividualNode> allIndividuals;
	public static ArrayList<GroupEdge> allEdges;
	public static ArrayList<IndividualNode> infectIndividuals;
	public static ArrayList<IndividualNode> migratingIndividuals;
	static ContinuousSpace <Object > individualSpace;
	static Network<IndividualNode> iNet;
	static Network<GroupNode> gNet;

	private static int numGroups ;
	private static int landSize ;
	public static ArrayList<IndividualNode> individualsToRemove;
	public static ArrayList<IndividualNode> individualsToAdd;


	public Context<Object> build(Context<Object> context){

		System.out.println("Running SIV percolation model");

		/********************************
		 * 								*
		 * initialize model parameters	*
		 * 								*
		 *******************************/

		mainContext = context; //static link to context
		allGroups = new ArrayList<GroupNode>();	
		allIndividuals = new ArrayList<IndividualNode>();	
		allEdges = new ArrayList<GroupEdge>();
		infectIndividuals = new ArrayList<IndividualNode>();
		migratingIndividuals = new ArrayList<IndividualNode>();
		individualsToRemove= new ArrayList<IndividualNode>();
		individualsToAdd= new ArrayList<IndividualNode>();
		Params p = new Params();

		numGroups = p.numberOfGroups;
		landSize = p.landscapeSize;

		System.out.println("Building landscape");

		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		//ContinuousSpace <Object > groupSpace 		= spaceFactory.createContinuousSpace("groupSpace", context , new RandomCartesianAdder <Object >(), new repast.simphony.space.continuous.StrictBorders(), landSize, landSize);
		individualSpace 	= spaceFactory.createContinuousSpace("individualSpace", context , new RandomCartesianAdder <Object >(), new repast.simphony.space.continuous.StrictBorders(), landSize, landSize);

		NetworkBuilder <Object > netBuilder1 = new NetworkBuilder <Object > ("groupNetwork", context , true); 
		netBuilder1.buildNetwork();
		NetworkBuilder <Object > netBuilder2 = new NetworkBuilder <Object > ("individualNetwork", context , false); 
		netBuilder2.buildNetwork();

		iNet = (Network <IndividualNode >)context.getProjection("individualNetwork");
		gNet = (Network <GroupNode >)context.getProjection("groupNetwork");

		/****************************************
		 * 				    			        *
		 * Adding Group Nodes to the landscape	*
		 * 					     		        *
		 * **************************************/

		System.out.println("--adding group nodes");

		//groups: random structure

		for (int j = 0; j < numGroups; j++){

			//add node
			Coordinate coord = new Coordinate(RandomHelper.nextDoubleFromTo((0),(landSize)), RandomHelper.nextDoubleFromTo(0,(landSize)));
			GroupNode gNode = new GroupNode(individualSpace,gNet,j,coord);
			allGroups.add(gNode);
			context.add(gNode);
			individualSpace.moveTo(gNode, coord.x,coord.y);
		}

		//groups: grid structure
		/*int xdim=Params.landscapeSize,ydim=Params.landscapeSize,groupNumb=0;
		double offset=100,xcoord=0, ycoord=0;
		while (ycoord<ydim){
			while(xcoord<xdim){
				Coordinate coord = new Coordinate(xcoord,ycoord);
				GroupNode gNode = new GroupNode(individualSpace,gNet,groupNumb++,coord);
				allGroups.add(gNode);
				context.add(gNode);
				individualSpace.moveTo(gNode, coord.x,coord.y);
				xcoord = xcoord+offset;
			}
			ycoord = ycoord+offset;
			xcoord = 0;
		}
		 */
		//landscape graph theory (connectivity measures)






		/********************************************
		 * 				        			        *
		 * Adding Individual Nodes to the landscape	*
		 * 					         		        *
		 * *****************************************/

		System.out.println("--adding individual nodes");

		//add individuals to groups + update group size
		int idCount=0;
		for (GroupNode gn : allGroups){

			for(int k =0;k<Params.groupSize;k++){
				//add individuals
				Coordinate coord = new Coordinate(Params.landscapeSize+1,Params.landscapeSize+1); 
				while(Math.abs(coord.x)>Params.landscapeSize || Math.abs(coord.y)>Params.landscapeSize || coord.x<0 || coord.y<0){
					coord = new Coordinate(gn.coord.x+(RandomHelper.nextDouble()-0.5)*5,gn.coord.y+(RandomHelper.nextDouble()-0.5)*5);
				}
				IndividualNode newInd = new IndividualNode(individualSpace,iNet,gn);
				gn.getIndividualNodes().add(newInd);
				context.add(newInd);
				individualSpace.moveTo(newInd, coord.x,coord.y);
				allIndividuals.add(newInd);
			}

			//record size of the group
			gn.setGroupSize(gn.getIndividualNodes().size());

			//infection initialization
			if(gn.getID()==0){
				for(IndividualNode nn : gn.getIndividualNodes()){
					nn.iStatus=1;
				}
				//gn.getIndividualNodes().get(0).iStatus=1;
			}

		}

		/************************************
		 * 							        *
		 * Adding Edges between groups    	*
		 * 							        *
		 * *********************************/	

		System.out.println("--adding group edges");


		for(GroupNode nodeStart : allGroups){

			ContinuousWithin<GroupNode> n = new ContinuousWithin(context,nodeStart,Params.maxRadiusOfConnections);
			Iterable<GroupNode> queryNear = n.query();
			ArrayList<GroupNode> nearNodesA = new ArrayList<GroupNode>();

			for(Object unk: queryNear){
				try{
					GroupNode nn = (GroupNode) unk;
					nearNodesA.add(nn);

				}catch (ClassCastException e){

				}
			}

			for(GroupNode no : nearNodesA){
				if(no.id!=nodeStart.id){
					//add edge
					GroupEdge edge_focal = new GroupEdge(nodeStart,no,true,no.getCoord().distance(nodeStart.getCoord()));
					//context.add(edge_focal);
					gNet.addEdge(edge_focal);
				}
			}
		}

		/****************************************
		 * 							            *
		 * Adding Edges between individuals    	*
		 * 							            *
		 * *************************************/		

		System.out.println("--adding individual edges");

		for(GroupNode group : allGroups){

			//add edges between individuals in the groups
			for(int start=0;start<group.size;start++){
				IndividualNode focal = group.myIndividuals.get(start);
				Collections.shuffle(group.myIndividuals);
				for (int i =0;i<Params.individualEdges;i++){
					IndividualNode target = group.myIndividuals.get(i);
					IndividualEdge edge = new IndividualEdge(focal,target,false,1);
					context.add(edge);
					iNet.addEdge(edge);
				}
			}
		}


		/************************************
		 * 							        *
		 * Scheduler to synchronize runs	*
		 * 							        *
		 * *********************************/

		
		//observer takes care of recoridng information from the simulation
		Observer obs = new Observer();

		//executor takes care of the processing of the schedule
		Executor executor = new Executor();
		createSchedule(executor, obs);

		return context;

	}


	private void createSchedule(Executor executor, Observer obs){

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();

		ScheduleParameters agentStepParams_Nodes = ScheduleParameters.createRepeating(1, 1, 6); //start, interval, priority (high number = higher priority)
		schedule.schedule(agentStepParams_Nodes,executor,"processNodes");

		ScheduleParameters agentStepParams_Infection = ScheduleParameters.createRepeating(1, 1, 5); //start, interval, priority (high number = higher priority)
		schedule.schedule(agentStepParams_Infection,executor,"spreadInfection");

		ScheduleParameters agentStepParams_Migration = ScheduleParameters.createRepeating(1, 1, 4); //start, interval, priority (high number = higher priority)
		schedule.schedule(agentStepParams_Migration,executor,"processMigration");
		
		ScheduleParameters observer_recording = ScheduleParameters.createRepeating(1, 1, 3); //start, interval, priority (high number = higher priority)
		schedule.schedule(observer_recording,obs,"step");
		
		ScheduleParameters stop = ScheduleParameters.createAtEnd(ScheduleParameters.LAST_PRIORITY);
		schedule.schedule(stop, obs, "output");

	}

	public synchronized static void addInfected(IndividualNode n){
		infectIndividuals.add(n);
	}
	public static void spreadInfection(){
		for(IndividualNode n:infectIndividuals){
			n.iStatus=1;
		}
		infectIndividuals.clear();
	}

	public synchronized static void addMigratingInd(IndividualNode ind){
		migratingIndividuals.add(ind);
	}

	public static void performMigration() {

		//process migrations
		for(IndividualNode n:migratingIndividuals){

			//choose group to migrate to
			Iterable<RepastEdge<GroupNode>> ge = gNet.getOutEdges(n.getMyGroup());
			ArrayList<RepastEdge<GroupNode>> ai = new ArrayList<RepastEdge<GroupNode>>();
			for(RepastEdge<GroupNode> i : ge){
				ai.add(i);
			}
			if(ai.size()>0){
				Collections.shuffle(ai);
				GroupNode newGroup = ai.get(0).getTarget();

				//perform move (SEVER AND CREAT NEW NETWORK ASSOCIATIONS)
				//sever old connections
				removeEdges(n); 
				//move physically to new group
				n.getMyGroup().myIndividuals.remove(n);
				n.setMyGroup(newGroup);
				newGroup.myIndividuals.add(n);
				Coordinate coord = new Coordinate(Params.landscapeSize+1,Params.landscapeSize+1); 
				while(Math.abs(coord.x)>Params.landscapeSize || Math.abs(coord.y)>Params.landscapeSize || coord.x<0 || coord.y<0){
					coord = new Coordinate(newGroup.coord.x+(RandomHelper.nextDouble()-0.5)*5,newGroup.coord.y+(RandomHelper.nextDouble()-0.5)*5);
				}
				individualSpace.moveTo(n, coord.x,coord.y);

				//create new associations
				Collections.shuffle(newGroup.myIndividuals);
				for (int i =0;i<Params.individualEdges;i++){
					IndividualNode target = newGroup.myIndividuals.get(i);
					connectTwoIndividuals(n,target);
				}
			}
		}

		migratingIndividuals.clear();

		//process transmission
		transmissionStep(); 

		//process deaths
		removeIndividuals();

		//process new individuals
		processNewIndividuals();

		//process group nodes for visualization
		for(GroupNode gn:allGroups){
			gn.step();
		}
		//}

	}

	private static void removeEdges(IndividualNode n){
		Iterable<RepastEdge<IndividualNode>> listOfEdges = iNet.getEdges(n);
		ArrayList<RepastEdge<IndividualNode>> al = new ArrayList<RepastEdge<IndividualNode>>();
		for(RepastEdge<IndividualNode> ee : listOfEdges){
			al.add(ee);
		}
		for(RepastEdge<IndividualNode> ie : al){
			iNet.removeEdge(ie);
			//mainContext.remove(ie);
		}

	}

	private static void connectTwoIndividuals(IndividualNode startN, IndividualNode endN){
		IndividualEdge edge = new IndividualEdge(startN,endN,false,1);
		mainContext.add(edge);
		iNet.addEdge(edge);
	}

	public static ArrayList<IndividualNode> getAllIndividuals(){
		return allIndividuals;
	}
	
	public static ArrayList<GroupNode> getAllGroups(){
		return allGroups;
	}

	public static Network getGroupNet(){
		return gNet;
	}

	public static ArrayList<IndividualNode> getIndividualsToRemove() {
		return individualsToRemove;
	}

	private static void removeIndividuals(){

		for(IndividualNode nn : individualsToRemove){
			//removeEdges(nn);
			nn.myGroup.getIndividualNodes().remove(nn);
			mainContext.remove(nn);
			allIndividuals.remove(nn);
		}

		individualsToRemove.clear();
	}
	private static void transmissionStep(){

		//transmission
		//find all associates
		Iterable<RepastEdge<IndividualNode>> listOfEdges = iNet.getEdges();

		for(RepastEdge<IndividualNode> ie : listOfEdges){
			IndividualNode source = ie.getSource();
			IndividualNode target = ie.getTarget();
			int inf = source.getStatus()+target.getStatus();
			if(inf==1){
				if(RandomHelper.nextDouble()<=Params.pTrans+0){
					if(target.getStatus()==0)ModelSetup.addInfected(target);
					if(source.getStatus()==0)ModelSetup.addInfected(source);
				}
			}
		}
	}


	public static void addNewIndividual(IndividualNode newInd) {

		//create new individual
		newInd.myGroup.getIndividualNodes().add(newInd);
		mainContext.add(newInd);

		Coordinate coord = new Coordinate(Params.landscapeSize+1,Params.landscapeSize+1); 
		while(Math.abs(coord.x)>Params.landscapeSize || Math.abs(coord.y)>Params.landscapeSize || coord.x<0 || coord.y<0){
			coord = new Coordinate(newInd.myGroup.coord.x+(RandomHelper.nextDouble()-0.5)*5,newInd.myGroup.coord.y+(RandomHelper.nextDouble()-0.5)*5);
		}

		individualSpace.moveTo(newInd, coord.x,coord.y);

		allIndividuals.add(newInd);

		//create new associations
		Collections.shuffle(newInd.myGroup.myIndividuals);
		for (int i =0;i<Params.individualEdges;i++){
			try{
				IndividualNode target = newInd.myGroup.myIndividuals.get(i);
				connectTwoIndividuals(newInd,target);
			} catch(IndexOutOfBoundsException ee){
				break;
			}
			//double d = target.getCoord().distance(newInd.getCoord());
			//if(d>100){
			//	System.out.println("what?");
			//}else{

			//}
		}
	}


	public static ArrayList<IndividualNode> getIndividualsToAdd() {
		return individualsToAdd;
	}

	public static void processNewIndividuals() {

		for(IndividualNode nn : individualsToAdd){
			addNewIndividual(nn);
		}
		individualsToAdd.clear();

	}

}
