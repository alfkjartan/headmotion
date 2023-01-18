/** Class which represents a joint model of varying number of degrees of
 * freedom.
 *
 * Works only for twists which are defined either with w=[0,0,0] and v in the 
 * translation direction, or with v = -w x q, where q is a point on the axis 
 * of rotation.
 *
 * 2002-11-29
 * @author: Kjartan Halvorsen
 */

package kha.math;

import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;

public class JointModel {
    private DoubleMatrix2D twists;
    private DoubleMatrix1D phi;
    private int dof;

    private static DoubleFactory1D f1 = DoubleFactory1D.dense;
    private static DoubleFactory2D f2 = DoubleFactory2D.dense;
    private static DoubleFactory3D f3 = DoubleFactory3D.dense;
    private static Blas blas = SeqBlas.seqBlas;
    private static Algebra alg = Algebra.DEFAULT;

    public JointModel(DoubleMatrix2D twists) {
	this.twists=twists;
	this.dof = twists.columns();
	this.phi = f1.make(dof);
    }

    public void setState(DoubleMatrix1D x) {
	this.phi.assign(x);
    }

    public DoubleMatrix2D trf() {
	DoubleMatrix2D G = f2.identity(4);
	DoubleMatrix2D Gi = f2.identity(4);
	DoubleMatrix2D xihat = f2.make(4,4);
	
	for (int i=0; i<dof; i++) {
	    expTwist(twists.viewColumn(i),phi.get(i),G,Gi,xihat);
	}
	
	return G;
    }
	
    public DoubleMatrix3D jacobian() {
	DoubleMatrix2D G = f2.identity(4);
	DoubleMatrix3D Gs = f3.make(4,4,dof);
	DoubleMatrix3D xihats = f3.make(4,4,dof);
	
	for (int i=0; i<dof; i++) {
	    expTwist(twists.viewColumn(i),phi.get(i),G,
		     Gs.viewColumn(i),xihats.viewColumn(i));
	}
	
	DoubleMatrix3D before = f3.make(4,4,dof);
	DoubleMatrix3D after = f3.make(4,4,dof);
	before.viewColumn(0).assign(f2.identity(4));
	after.viewColumn(dof-1).assign(f2.identity(4));
	for (int i=0; i<dof-1; i++) {
	    before.viewColumn(i+1).assign(alg.mult(before.viewColumn(i),
						   Gs.viewColumn(i+1)));
	    after.viewColumn(dof-2-i).assign(alg.mult(Gs.viewColumn(dof-1-i),
				      after.viewColumn(dof-1-i)));
	}

	DoubleMatrix3D dgdx=f3.make(4,4,dof);
	for (int i=0; i<dof; i++) {
	    dgdx.viewColumn(i).assign(alg.mult(alg.mult(before.viewColumn(i),
					     xihats.viewColumn(i)),
					     after.viewColumn(i)));
	}

	return dgdx;
    }
					  

    private static void expTwist(DoubleMatrix1D tw, double th,
				 DoubleMatrix2D G, DoubleMatrix2D Gi,
				 DoubleMatrix2D xihat) {
	Gi.assign(0);
	Gi.setQuick(3,3,1);
	
	// The rotation matrix
	DoubleMatrix2D R;
	if (alg.norm2(tw.viewPart(3,3))==0) {
	    // Pure translation
	    R=f2.identity(3);
	    
	    blas.daxpy(th,tw.viewPart(0,3),
		       Gi.viewColumn(3).viewPart(0,3));
	} else {
	    R = f2.make(3,3);
	    rodrigues(tw.viewPart(3,3),th,R);

	    // The translation part
	    DoubleMatrix2D IR = f2.identity(3);
	    blas.daxpy(-1.0,R,IR);
	    DoubleMatrix1D  q = 
		alg.mult(hat(tw.viewPart(3,3)),tw.viewPart(0,3));
	    blas.dgemv(false,1.0,IR,q,0,Gi.viewColumn(3).viewPart(0,3));
	}
	Gi.viewPart(0,0,3,3).assign(R);

	
	// Multiply with proximal trf matrix G
	// OBS. Assumes that element (3,3) of G is 1

	DoubleMatrix2D Gtmp = alg.mult(G,Gi);
	G.assign(Gtmp);

	// Set xihat
	xihat.viewPart(0,0,3,3).assign(hat(tw.viewPart(3,3)));
	xihat.viewColumn(3).viewPart(0,3).assign(tw.viewPart(0,3));
    }


    private static void rodrigues(DoubleMatrix1D w, double th, 
				  DoubleMatrix2D R) {
	R.assign(f2.identity(3));

	DoubleMatrix2D what = hat(w);
	blas.daxpy(Math.sin(th),what,R);

	blas.daxpy((1-Math.cos(th)),alg.pow(what,2),R);
    }

    public static DoubleMatrix2D hat(DoubleMatrix1D w) {
	DoubleMatrix2D tmp = f2.make(3,3);

	tmp.setQuick(0,1,-w.getQuick(2));
	tmp.setQuick(0,2,w.getQuick(1));
	tmp.setQuick(1,0,w.getQuick(2));
	tmp.setQuick(1,2,-w.getQuick(0));
	tmp.setQuick(2,0,-w.getQuick(1));
	tmp.setQuick(2,1,w.getQuick(0));

	return tmp;
	
    }


    public static void main (String[] args) {
	// Construct som twists
	DoubleMatrix2D tws = f2.identity(6);

	// And a jointmodel
	JointModel jm = new JointModel(tws);

	if (false) {
      	jm.setState(f1.make(new double[]{1,0,0,0,0,0}));
	DoubleMatrix2D G = jm.trf();
	System.out.println("x(1)=1");
	System.out.println(G);
	
	jm.setState(f1.make(new double[]{1,2,3,0,0,0}));
	G = jm.trf();
	System.out.println("x=[1 2 3 0 0 0]");
	System.out.println(G);
	
	jm.setState(f1.make(new double[]{1,2,3,0,0,0}));
	G = jm.trf();
	System.out.println("x=[1 2 3 0 0 0]");
	System.out.println(G);
	
	jm.setState(f1.make(new double[]{1,2,3,Math.PI/3,0,0}));
	G = jm.trf();
	System.out.println("x=[1 2 3 pi 0 0]");
	System.out.println(G);
	
	jm.setState(f1.make(new double[]{1,2,3,0,Math.PI/3,0}));
	G = jm.trf();
	System.out.println("x=[1 2 3 0 pi 0]");
	System.out.println(G);
	
	jm.setState(f1.make(new double[]{1,2,3,0,0,Math.PI/3}));
	G = jm.trf();
	System.out.println("x=[1 2 3 0 0 pi]");
	System.out.println(G);
	}

	jm.setState(f1.make(new double[]{1,2,3,
					 Math.PI/3,Math.PI/4,Math.PI/6}));
	DoubleMatrix3D jacob=jm.jacobian();
	
	System.out.println(jacob.viewColumn(0));
	System.out.println(jacob.viewColumn(1));
	System.out.println(jacob.viewColumn(2));
	System.out.println(jacob.viewColumn(3));
	System.out.println(jacob.viewColumn(4));
	System.out.println(jacob.viewColumn(5));
    }    
}
