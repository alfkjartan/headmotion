/** 
 * Interface for classes used as state space models by the Kalman class.
 *
 * 2002-11-25
 * @author: Kjartan Halvorsen
 */

package kha.math;

import cern.colt.matrix.*;

public interface StateSpaceModel { 

    public void sysfunc(DoubleMatrix1D x, DoubleMatrix1D xn,
			DoubleMatrix2D F);

    public void obsfunc(DoubleMatrix1D x, DoubleMatrix1D y,
			DoubleMatrix1D yhat,DoubleMatrix2D H);

    public void noisefunc(DoubleMatrix1D x, DoubleMatrix2D G);

    public void R1(int t,DoubleMatrix2D R);

    public void R2(int t,DoubleMatrix2D R);

    public boolean isSysfuncTimeInvariant();

    public boolean isObsfuncTimeInvariant();
}
    
