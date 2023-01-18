/**
 * Assumes that the set of MotionData object each contains a single point. 
 * Plots these in the same Plot window.
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-04-14
 */

package kha.track;

import java.util.Vector;
import cern.colt.matrix.*;
import kha.math.*;

public class PlotSinglePointsDisplay extends Display {
    
    /**
     * Plots the single data point in each MotionData object in the 
     * same figure
     * @param  vec  the set of MotionData objects
     */
    public void displaySet(Vector vec) {
	DoubleMatrix1D data = DoubleFactory1D.dense.make(vec.size()); 
	String unit = "";
	for (int i=0; i<vec.size(); i++) {
	    MotionData md = (MotionData) vec.elementAt(i);
	    data.setQuick(i,md.getData().getQuick(0,0));
	    String[] units = md.getUnits();
	    unit = units[0];
	}
	Plotter aplot = new Plotter();
	aplot.addData(data);
	aplot.getCurrentPlot().setYLabel(unit);
    }
}

