/** Computes the mean absolute value of the scaled power 
 * for a 6dof rigid motion.  
 * Considers only rotational motion.
 * Subclass of the AbsoluteScaledPower class
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-04-15
 */

package kha.track;

import cern.colt.matrix.*;
import java.util.Hashtable;

public class AbsoluteScaledWork extends AbsoluteScaledPower {

    public String toString() {
	return "Scaled mechanical work";
    }

    public MotionData transform (MotionData mdata) 
	throws MalformedDataException {

	MotionData power = super.transform(mdata);
	
	try {
	    DoubleMatrix2D pw = power.getData();

	    DoubleMatrix2D work = f2.make(1,1);
	    work.setQuick(0,0,pw.zSum()/((double) pw.rows()));
	    
	    MotionData nmd = new MotionData(work,power.getAttributes(),
				  power.getSamplingTime(), 
				  new String[] {""});
	    nmd.setTestType(mdata.getTestType());
	    nmd.setDescription(mdata.toString());

	    return nmd;
	    
	} catch (IndexOutOfBoundsException ibe) {
	    throw new MalformedDataException(ibe.getMessage());
	}
    }
}

	
