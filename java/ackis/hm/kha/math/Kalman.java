/** 
 *Class providing an extended Kalman filter
 *
 * 2001-10-28
 * @author: Kjartan Halvorsen
 */

package kha.math;

import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;

public class Kalman {

    private static DoubleFactory3D f3 = DoubleFactory3D.dense;
    private static DoubleFactory2D f2 = DoubleFactory2D.dense;
    private static DoubleFactory1D f1 = DoubleFactory1D.dense;
    private static Algebra a = Algebra.DEFAULT;
    private static Blas b = SeqBlas.seqBlas;

    private Kalman(){}

    public static int fix_interval(DoubleMatrix2D y, 
				    DoubleMatrix1D x0,
				    DoubleMatrix2D P0,
				    StateSpaceModel model,
				    DoubleMatrix2D xsmooth,
				    DoubleMatrix3D Psmooth) {
	int d = y.rows();     // The length of the observation vectors
	int N = y.columns();  // The data length

	int n = x0.size();    // The length of the state vector

	// Initializations
	DoubleMatrix1D xtt = f1.make(n);
	DoubleMatrix1D xt = f1.make(n);
	xt.assign(x0);
	DoubleMatrix2D Ft = f2.make(n,n);
	DoubleMatrix2D Ptt = f2.make(n,n);
	DoubleMatrix2D Pt = f2.make(n,n);
	Pt.assign(P0);

	DoubleMatrix1D ee = f1.make(d);
	DoubleMatrix1D yhat = f1.make(d);
	DoubleMatrix2D Ht = f2.make(d,n);
	DoubleMatrix2D R2 = f2.make(d,d);
	DoubleMatrix2D HPHR = f2.make(d,d);
	DoubleMatrix2D Gt = f2.make(n,n);
	DoubleMatrix2D Kt = f2.make(n,d);

	DoubleMatrix2D xhat = f2.make(n,N);
	DoubleMatrix3D Phat = f3.make(n,n,N);

	// Forward pass to obtain predicted estimates
	for (int t=0; t<N; t++) {
	    xhat.viewColumn(t).assign(xt);
	    Phat.viewColumn(t).assign(Pt);

	    model.obsfunc(xt,y.viewColumn(t),yhat,Ht);

	    // The measurement noise covariance
	    model.R2(t,R2);

	    // The Kalman gain
	    HPHR = a.mult(a.mult(Ht,Pt),a.transpose(Ht));
	    b.daxpy(1,R2,HPHR);
	    Kt = a.mult(a.mult(Pt,a.transpose(Ht)),a.inverse(HPHR));

	    // The innovation
	    ee.assign(y.viewColumn(t));
	    b.daxpy(-1,yhat,ee);

	    if (t<0) {
		System.err.println();
		System.err.println("innovations:");
		System.err.println(ee);
		System.err.println("y:");
		System.err.println(y.viewColumn(t));
		System.err.println("yhat:");
		System.err.println(yhat);
	    }

	    // The filter update
	    xtt.assign(xt);
	    b.daxpy(1,a.mult(Kt,ee),xtt);


	    // The estimate covariance update
	    Ptt.assign(Pt);
	    b.daxpy(-1,a.mult(a.mult(Kt,Ht),Pt),Ptt);

	    // Prediction
	    model.sysfunc(xtt,xt,Ft);

	    model.noisefunc(xtt,Gt);

	    Pt = a.mult(a.mult(Ft,Ptt),a.transpose(Ft));
	    b.daxpy(1,a.mult(Gt,a.transpose(Gt)),Pt);
	    
	}

	// Backward pass to obtain smoothed estimates
	xsmooth.viewColumn(N-1).assign(xtt);
	Psmooth.viewColumn(N-1).assign(Ptt);
	
	DoubleMatrix1D tmp = f1.make(n);
	DoubleMatrix1D xsm = f1.make(n);
	DoubleMatrix1D ex = f1.make(n);
	DoubleMatrix2D St = f2.make(n,n);
	DoubleMatrix2D Mt = f2.make(n,n);
	DoubleMatrix2D Psm = f2.make(n,n);
	DoubleMatrix2D eye = f2.identity(n);

	DoubleMatrix2D Ftinv = f2.make(n,n);
	boolean sysTI = false;
	if (model.isSysfuncTimeInvariant()) {
	    Ftinv = a.inverse(Ft);
	    sysTI = true;
	}

	for (int t=N-2;t>=0;t--) {
	    // Get Ft and Gt
	    if (!sysTI) {
		model.sysfunc(xhat.viewColumn(t),xt,Ft);
		model.noisefunc(xhat.viewColumn(t),Gt);
		Ftinv = a.inverse(Ft);
	    }
	    
	    St = a.mult(a.transpose(Gt),a.inverse(Phat.viewColumn(t+1)));
	    Mt.assign(eye);
	    b.daxpy(-1,a.mult(St,Gt),Mt);
	    
	    // The smoothed estimate
	    xsm.assign(xsmooth.viewColumn(t+1));
	    ex.assign(xsm);
	    b.daxpy(-1,xhat.viewColumn(t+1),ex);
	    b.daxpy(-1,a.mult(a.mult(Gt,St),ex),xsm);
	    xsmooth.viewColumn(t).assign(a.mult(Ftinv,xsm));

	    // Covariance update
	    Ptt.assign(eye);
	    b.daxpy(-1,a.mult(Gt,St),Ptt);
	    Ptt = a.mult(Ftinv,Ptt);
	    Pt = a.mult(Ftinv,Gt);

	    Psm = a.mult(a.mult(Pt,Mt),a.transpose(Pt));
	    b.daxpy(1,a.mult(a.mult(Ptt,Psmooth.viewColumn(t+1)),
			     a.transpose(Ptt)),Psm);

	    Psmooth.viewColumn(t).assign(Psm);
	}

	return 0;
    }
}

	    
 
