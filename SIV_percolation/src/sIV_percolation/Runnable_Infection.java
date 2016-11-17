package sIV_percolation;

public class Runnable_Infection implements Runnable {
	
ModelSetup ms;
	
	Runnable_Infection(ModelSetup e){
		ms = e;
	}
	
	@Override
	public void run(){
		Throwable thrown = null;
	    try {
	    	ms.performMigration();
	    } catch (Throwable e) {
	        thrown = e;
	        System.out.println("Problem lies in spread infection code code" + thrown);
	    } finally {
	    	return;
	    }
	}

}
