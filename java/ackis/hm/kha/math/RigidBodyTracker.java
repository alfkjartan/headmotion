/** 
 * Class for tracking the 6 DOF movement of a rigid body
 *
 * 2002-11-29
 * @author: Kjartan Halvorsen
 */

package kha.math;

import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;

public class RigidBodyTracker extends SixDOFTracker { 

    private JointModel jm;
    private DoubleMatrix2D p0;

    private DoubleFactory1D f1 = DoubleFactory1D.dense;
    private DoubleFactory2D f2 = DoubleFactory2D.dense;
    private DoubleFactory3D f3 = DoubleFactory3D.dense;
    private static Algebra a = Algebra.DEFAULT;
    private Blas blas = SeqBlas.seqBlas;

    public RigidBodyTracker(DoubleMatrix2D Ra, DoubleMatrix2D R2, double dt,
			    JointModel jm,DoubleMatrix2D p0) {
	super(Ra,R2,dt);
	this.jm = jm;
	this.p0 = p0;
    }

    public void obsfunc(DoubleMatrix1D x, DoubleMatrix1D y, 
			DoubleMatrix1D yhat, DoubleMatrix2D H) {

	jm.setState(x.viewPart(0,6));
	
	// The position of the markers
	DoubleMatrix2D pt = a.mult(jm.trf(),p0);
	KAlgebra.vec(pt.viewPart(0,0,3,pt.columns()),yhat);

	// The Jacobian of the kinematics map
	DoubleMatrix3D jacob = jm.jacobian();
	
	// The Jacobian of the observation function
	for (int i=0; i<6; i++) {
	    pt = a.mult(jacob.viewColumn(i),p0);
	    KAlgebra.vec(pt.viewPart(0,0,3,pt.columns()),H.viewColumn(i));
	}
	
	// Find missing markers in y (elements equal to zero).
	// Set corresponding rows of H to zero.
	for (int j=0; j<y.size(); j++) {
	    if (y.getQuick(j) == 0.0) {
		H.viewRow(j).assign(0.0);
	    }
	}
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
	
	// Construct a JointModel
	JointModel jm = new JointModel(f2.identity(6));

	// a few points. 
	DoubleMatrix2D p0 = f2.make(new double[]{2,2,2,1, 3,2,2,1,
						 2,3,2,1, 1,2,3,1},4);
 
	// Instantiate a state space model
	StateSpaceModel tracker = new RigidBodyTracker(Ra,R2,0.2,
						       jm, p0);
	
	//Check it out
	DoubleMatrix2D res1=f2.make(18,18);
	DoubleMatrix2D res2=f2.make(12,18);
	DoubleMatrix2D res3=f2.make(6,6);
	DoubleMatrix1D x0=f1.make(18,1.0);
	DoubleMatrix1D x1=f1.make(
				  new double[]{1,2,3,
					       Math.PI/3,Math.PI/4,Math.PI/6,
					       0,0,0,0,0,0,
					       0,0,0,0,0,0});
	DoubleMatrix1D y=f1.make(12);
	DoubleMatrix1D yhat=f1.make(12);
	
	// Test obsfunc
	tracker.obsfunc(x1,y,yhat,res2);
	System.out.println("Observation vector");
	System.out.println(yhat);
	System.out.println("H matrix");
	System.out.println(res2);
	
}	
}
    
