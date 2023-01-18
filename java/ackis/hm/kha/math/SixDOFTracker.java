/** 
 * Class for 6 DOF model used in tracking
 *
 * 2002-11-26
 * @author: Kjartan Halvorsen
 */

package kha.math;

import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;

public class SixDOFTracker implements StateSpaceModel { 
    private DoubleMatrix2D R1;
    private DoubleMatrix2D R2;
    private DoubleMatrix2D F;
    private DoubleMatrix2D G;
    private DoubleFactory2D factory = DoubleFactory2D.dense;
    private Blas blas = SeqBlas.seqBlas;

    public SixDOFTracker(DoubleMatrix2D Ra, DoubleMatrix2D r2, double dt) {

	// Construct the F matrix
	DoubleMatrix2D A = factory.make(new double[]{1,0,0,
						     dt,1,0,
						     0.5*dt*dt,dt,1},3);
	DoubleMatrix2D eye = factory.identity(6);

	F=KAlgebra.kron(A,eye);

	// Construct the process noise covariance, then factorize, and
	// make noise  unit variance.
	A = factory.make(new double[]{0.05*Math.pow(dt,5),
				      Math.pow(dt,4)/(8.0),
				      Math.pow(dt,3)/(6.0),
				      Math.pow(dt,4)/(8.0),
				      Math.pow(dt,3)/(3.0),
				      Math.pow(dt,2)/(2.0),
				      Math.pow(dt,3)/(6.0),
				      Math.pow(dt,2)/(2.0),
				      dt},3);
	R1=KAlgebra.kron(A,Ra);
	CholeskyDecomposition CD = new CholeskyDecomposition(R1);
	G=CD.getL();
	R1=factory.identity(18);

	R2 = r2;
    }

    public void sysfunc(DoubleMatrix1D x, DoubleMatrix1D xn,
			DoubleMatrix2D FF) {
	DoubleMatrix1D tmp=F.zMult(x,xn);
	FF.assign(F);
    }


    public void obsfunc(DoubleMatrix1D x, DoubleMatrix1D y, 
			DoubleMatrix1D yhat, DoubleMatrix2D H) {
	H.assign(KAlgebra.kron(factory.make(new double[]{1,0,0},1),
			       factory.identity(6)));
	DoubleMatrix1D tmp = H.zMult(x,yhat);
    }


    public void noisefunc(DoubleMatrix1D x, DoubleMatrix2D GG) {
	GG.assign(G);
    }

    public void R1(int t,DoubleMatrix2D R) {
	R.assign(R1);
    }

    public void R2(int t,DoubleMatrix2D R) {
	R.assign(R2);
    }

    public boolean isSysfuncTimeInvariant() {
	return true;
    }
	
    public boolean isObsfuncTimeInvariant() {
	return false;
    }

    public static void main (String[] args) {
	DoubleFactory1D f1 = DoubleFactory1D.dense;
	DoubleFactory2D f2 = DoubleFactory2D.dense;

	// Construct a noise intensity matrix
	DoubleMatrix1D ra = f1.make(new double[]{1,2,3,4,5,6});
	DoubleMatrix2D Ra = f2.diagonal(ra);
	
	// Construct a measurement noise matrix
	DoubleMatrix1D r2 = f1.make(new double[]{6,5,4,3,2,1});
	DoubleMatrix2D R2 = f2.diagonal(r2);
	
	// Instantiate a state space model
	StateSpaceModel tracker = new SixDOFTracker(Ra,R2,0.2);
	
	//Check it out
	DoubleMatrix2D res1=f2.make(18,18);
	DoubleMatrix2D res2=f2.make(6,18);
	DoubleMatrix2D res3=f2.make(6,6);
	DoubleMatrix1D x0=f1.make(18,1.0);
	DoubleMatrix1D x1=f1.make(18);
	DoubleMatrix1D y=f1.make(6);
	DoubleMatrix1D yhat=f1.make(6);
	
	// Test sysfunc
	tracker.sysfunc(x0,x1,res1);
	System.out.println("Updated state vector");
	System.out.println(x1);
	System.out.println("F matrix");
	System.out.println(res1);
	
	// Test obsfunc
	tracker.obsfunc(x1,y,yhat,res2);
	System.out.println("Observation vector");
	System.out.println(yhat);
	System.out.println("H matrix");
	System.out.println(res2);
	
	// Test R1
	tracker.R1(1,res1);
	System.out.println("R1 matrix");
	System.out.println(res1);
	
	// Test R2
	tracker.R2(1,res3);
	System.out.println("R2 matrix");
	System.out.println(res3);

	// Test the cholesky factorization
	tracker.noisefunc(x0,res1);
	System.out.println("G matrix");
	System.out.println(res1);
	System.out.println("R1 matrix");
	Algebra a = new Algebra();
	System.out.println(a.mult(res1,a.transpose(res1)));

}	
}
    
