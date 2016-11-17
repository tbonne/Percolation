package sIV_percolation;

public class Runnable_Node implements Runnable {
	
IndividualNode node;
	
	Runnable_Node(IndividualNode n){
		node = n;
	}
	
	@Override
	public void run(){
		Throwable thrown = null;
	    try {
		node.step();
	    } catch (Throwable e) {
	        thrown = e;
	        System.out.println("Problem lies in node update code" + thrown);
	    } finally {
	    	return;
	    }
	}

}
