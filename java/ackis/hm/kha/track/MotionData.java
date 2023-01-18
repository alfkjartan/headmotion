/**
 * Encapsulation of motion data. The class contains time series (data) and
 * attributes to describe them.
 *
 * @author    Kjartan Halvorsen
 * @version   1.0  2003-04-08
 */

package kha.track;

import cern.colt.matrix.*;
import java.util.Hashtable;

public class MotionData { 
    
    private DoubleMatrix2D mdata;
    private Hashtable attributes;
    private double samplingTime;
    private String[] units;
    private String description = "";
    private String testtype = "";

    public MotionData(DoubleMatrix2D d, Hashtable attrs,
		      double st, String[] u) {
	mdata = d;
	attributes = attrs;
	samplingTime = st;
	units = u;
    }

    public DoubleMatrix2D getData() {
	return mdata;
    }

    public Hashtable getAttributes() {
	return attributes;
    }

    public double getSamplingTime(){
	return samplingTime;
    }

    public String[] getUnits(){
	return units;
    }

    public int frames() {
	return this.mdata.rows();
    }

    public String toString() {
	return this.description;
    }

    public void setDescription(String d) {
	this.description = d;
    }

    public void setTestType(String t) {
	this.testtype = t;
    }

    public String getTestType() {
	return this.testtype;
    }

    /**
     * Returns a copy of this object
     *
     * @return    Copy of this MotionData object.
     */
    public MotionData copy() {
	return new MotionData(mdata,attributes,samplingTime,units);
    }

}
    
