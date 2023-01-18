/** Computes the absolute value of the scaled power for a 6dof rigid motion. 
 * Considers only rotational motion.
 * Implementation of the DataTransform interface. 
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-04-08
 */

package kha.track;

import cern.colt.matrix.*;
import java.util.Hashtable;

public class AbsoluteScaledPower implements DataTransform {

    protected static DoubleFactory1D f1 = DoubleFactory1D.dense;
    protected static DoubleFactory2D f2 = DoubleFactory2D.dense;

    public String toString() {
	return "Scaled mechanical power";
    }

    public MotionData transform (MotionData mdata) 
	throws MalformedDataException {
	
	int frames_in_half_sec = (int) (0.5 / mdata.getSamplingTime());

	try {
	    DoubleMatrix2D mdd = mdata.getData().viewDice();

	    if (mdd.rows()<frames_in_half_sec) 
		return mdata;
	
	    DoubleMatrix2D md = mdd.viewPart(frames_in_half_sec,0,
					     mdd.rows()-frames_in_half_sec,
					     mdd.columns());
	    
	    DoubleMatrix1D ph = md.viewColumn(3);
	    DoubleMatrix1D th = md.viewColumn(4);
	    DoubleMatrix1D ps = md.viewColumn(5);

	    DoubleMatrix1D phdot = md.viewColumn(9);
	    DoubleMatrix1D thdot = md.viewColumn(10);
	    DoubleMatrix1D psdot = md.viewColumn(11);

	    DoubleMatrix1D phddot = md.viewColumn(15);
	    DoubleMatrix1D thddot = md.viewColumn(16);
	    DoubleMatrix1D psddot = md.viewColumn(17);

	    DoubleMatrix1D omega = f1.make(3);
	    DoubleMatrix1D omegadot = f1.make(3);
	    
	    DoubleMatrix2D scaledpow = f2.make(md.rows(),1);

	    for (int i=0; i<md.rows(); i++) {
		omega.setQuick(0,
			       phdot.getQuick(i)*Math.cos(th.getQuick(i)) * 
			       Math.cos(ps.getQuick(i)) +
			       thdot.getQuick(i)*Math.sin(ps.getQuick(i)));
		omega.setQuick(1,
			       -phdot.getQuick(i)*Math.cos(th.getQuick(i)) * 
			       Math.sin(ps.getQuick(i)) +
			       thdot.getQuick(i)*Math.cos(ps.getQuick(i)));
		omega.setQuick(2,
			       phdot.getQuick(i)*Math.sin(th.getQuick(i)) +
			       psdot.getQuick(i));

		omegadot.setQuick(0,
				  phddot.getQuick(i)*Math.cos(th.getQuick(i)) *
				  Math.cos(ps.getQuick(i)) -
				  phdot.getQuick(i)*thdot.getQuick(i)*
				  Math.sin(th.getQuick(i))*
				  Math.cos(ps.getQuick(i)) -
				  phdot.getQuick(i)*psdot.getQuick(i)*
				  Math.cos(th.getQuick(i))*
				  Math.sin(ps.getQuick(i)) +
				  thddot.getQuick(i)*Math.sin(ps.getQuick(i)) +
				  thdot.getQuick(i)*psdot.getQuick(i)*
				  Math.cos(ps.getQuick(i)));
		omegadot.setQuick(1,
				  -phddot.getQuick(i)*
				  Math.cos(th.getQuick(i)) *
				  Math.sin(ps.getQuick(i)) +
				  phdot.getQuick(i)*thdot.getQuick(i)*
				  Math.sin(th.getQuick(i))*
				  Math.sin(ps.getQuick(i)) -
				  phdot.getQuick(i)*psdot.getQuick(i)*
				  Math.cos(th.getQuick(i))*
				  Math.cos(ps.getQuick(i)) +
				  thddot.getQuick(i)*Math.cos(ps.getQuick(i)) -
				  thdot.getQuick(i)*psdot.getQuick(i)*
				  Math.sin(ps.getQuick(i)));
		omegadot.setQuick(2,
				  phddot.getQuick(i)*Math.sin(th.getQuick(i)) +
				  phdot.getQuick(i)*thdot.getQuick(i)*
				  Math.cos(th.getQuick(i)) + 
				  psddot.getQuick(i));

		scaledpow.setQuick(i,0,Math.abs(omega.zDotProduct(omegadot)));
	    
		if (i<-1) {
		    System.err.println("omega");
		    System.err.println(omega);
		    System.err.println("omegadot");
		    System.err.println(omegadot);
		}
	    }

	    MotionData nmd = new MotionData(scaledpow,mdata.getAttributes(),
				  mdata.getSamplingTime(), 
				  new String[] {"g/(m2s)"});

	    nmd.setTestType(mdata.getTestType());
	    nmd.setDescription(mdata.toString());

	    return nmd;

	} catch (IndexOutOfBoundsException ibe) {
	    throw new MalformedDataException(ibe.getMessage());
	}
    }
}

	
