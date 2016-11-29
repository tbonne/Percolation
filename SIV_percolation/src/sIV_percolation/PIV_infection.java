package sIV_percolation;

import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.RandomGenerator;

public class PIV_infection {
	
	//private double latent_time;
	//private double width_time;
	//private double center_time;
	//private double spread_time;
	private double rate;
	private double shape;
	private double alpha;
	//TruncatedNormal tn;
	RandomGenerator rg;
	GammaDistribution gamma;
	private int t;

	public PIV_infection(double r,double s, double a){
		//latent_time = l;
		//width_time = w;
		//center_time = c;
		//spread_time = s;
		alpha = a;
		rate = r;
		shape = s;
		//tn = new TruncatedNormal(rg,c,s,l,l+w);
		gamma = new GammaDistribution(r,s);
	}
	
	/*******************transmission probability******************************/
	
	public double getTransProb(){
		return alpha*gamma.density(t);
	}
	
	/*******************get and set methods***********************************/

//	public double getLatent(){
//		return latent_time;
//	}
//	public double getWidth(){
//		return width_time;
//	}
//	public double getCenter(){
//		return center_time;
//	}
//	public double getSpread(){
//		return spread_time;
//	}
	public double getAlpha(){
		return alpha;
	}
	public double getRate(){
		return rate;
	}
	public double getShape(){
		return shape;
	}
	public void stepT(){
		t++;
	}
	public int getStep(){
		return t;
	}
}
