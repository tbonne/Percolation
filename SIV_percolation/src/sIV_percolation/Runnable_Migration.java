package sIV_percolation;

public class Runnable_Migration implements Runnable {
	
ModelSetup ms;
	
	Runnable_Migration(ModelSetup e){
		ms = e;
	}
	
	@Override
	public void run(){
		Throwable thrown = null;
	    try {
	    	ms.spreadInfection();
	    } catch (Throwable e) {
	        thrown = e;
	        System.out.println("Problem lies in spread infection code code" + thrown);
	    } finally {
	    	return;
	    }
	}

}
