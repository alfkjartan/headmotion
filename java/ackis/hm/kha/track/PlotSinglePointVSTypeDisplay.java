/**
 * Plots single points according to description.
 * Assumes that the set of MotionData object each contains a single point. 
 * Plots these in the same Plot window.
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-04-30
 */

package kha.track;

import java.util.*;
import cern.colt.matrix.*;
import cern.colt.matrix.doublealgo.Sorting;
import cern.colt.list.*;
import cern.jet.stat.*;
import kha.math.*;

public class PlotSinglePointVSTypeDisplay extends Display {

    private Plotter aplot = null;

    /**
     * Plots the single data point in each MotionData object in the 
     * same figure, according to the description.
     * @param  vec  either a vector of MotionData objects, or 
     *              a vector of vectors of MotionData objects.
     *              If the first element is a String, it will be
     *              used as a legend
     */
    public void displaySet(Vector vec) {
	displaySet(vec,"");
    }

    /**
     * Plots the single data point in each MotionData object in the 
     * same figure, according to the description.
     * @param  vec  either a vector of MotionData objects, or 
     *              a vector of vectors of MotionData objects.
     *              If the first element is a String, it will be
     *              used as a legend
     * @param  title the title of the Plot
     */
    public void displaySet(Vector vec, String title) {
	if ((vec != null) && (vec.size() > 0)) {
	    Object o = vec.elementAt(0);
	    if (o instanceof MotionData) 
		displaySeries(vec,"");
	else if (o instanceof String) 
	    displaySeries(vec.subList(1,vec.size()),
			  (String) vec.elementAt(0));
	else if (o instanceof Vector) {
		for (Iterator it = vec.iterator(); it.hasNext();)
		    displaySet((Vector) it.next());
	    }
	}
	
	if (aplot != null ) 
	    aplot.getCurrentPlot().setTitle(title);

    }

    protected void displaySeries(List vec, String legend) {
	DoubleMatrix2D data = DoubleFactory2D.dense.make(vec.size(),2); 
	String unit = "";
	Vector types = new Vector(vec.size());
	for (int i=0; i<vec.size(); i++) {
	    MotionData md = (MotionData) vec.get(i);
	    data.setQuick(i,1,md.getData().getQuick(0,0));
	    String type = md.getTestType();
	    if (types.indexOf(type) == -1) { // not found
	    	types.add(type);
	    }
	    data.setQuick(i,0, types.indexOf(type));
	    
	    System.err.println(legend + "\t" + type + "\t" + 
			       md.getData().getQuick(0,0));

	    String[] units = md.getUnits();
	    unit = units[0];
	}

	if (aplot == null)
	     aplot = new Plotter();

	aplot.getCurrentPlot().setConnected(false);
	aplot.getCurrentPlot().setGrid(false);
	aplot.setYLog(true);
	aplot.addData(data);
	aplot.getCurrentPlot().setYLabel(unit);
	aplot.getCurrentPlot().setMarksStyle("various");
	aplot.getCurrentPlot().addLegend(
			    aplot.getCurrentPlot().getNumDataSets()-1,
			    legend);
	for (int i=0; i<types.size(); i++) {
	    aplot.getCurrentPlot().addXTick((String) 
					    types.elementAt(i), (double) i);
	}
	aplot.repaint();

	// Calculate the mean and range of each type
	System.out.println();
	System.out.println(legend);
	for (int i=0; i<types.size(); i++) {
	    
	    DoubleArrayList dtt = new DoubleArrayList();
	    for (int j=0; j<data.rows(); j++) {
		if (data.getQuick(j,0) == i)
		    dtt.add(data.getQuick(j,1));
	    }
	    dtt.sort();
	    int ntt = dtt.size();

	    // Print out median and range 
	    System.out.println(types.elementAt(i) + ": median=" +
			       Descriptive.median(dtt) + "   range=" +
			       (dtt.getQuick(ntt-1) - dtt.getQuick(0))); 
	}
	    
    }


    public void setYLabel(String label) {
	aplot.getCurrentPlot().setYLabel(label);
    }
}

