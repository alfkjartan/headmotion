/**
 * Plots single points. Assumes that the set of MotionData objects contain\
 * one data point each, and plots two and two at the same position on
 * the x-axis. Useful for displaying two different groups.
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-05-05
 */

package kha.track;

import java.util.*;
import cern.colt.matrix.*;
import kha.math.*;

public class PlotPairsDisplay extends Display {
    
    private Plotter theplot;

    /**
     * Plots the single data point in each MotionData object in the 
     * same figure, according to the description.
     * @param  vec  the set of MotionData objects
     */
    public void displaySet(Vector vec) {
	displaySet(vec,"");
    }

    /**
     * Plots the single data point in each MotionData object in the 
     * same figure, according to the description.
     * @param  vec  the set of MotionData objects
     * @param  title  the title of the plot
     */
    public void displaySet(Vector vec, String title) {
	DoubleMatrix2D data1 = DoubleFactory2D.dense.make(vec.size()/2,2); 
	DoubleMatrix2D data2 = DoubleFactory2D.dense.make(vec.size()/2,2); 
	String unit = "";
	int i=0;
	for (Iterator it=vec.iterator(); it.hasNext(); ) {
	    MotionData md1 = (MotionData) it.next();
	    MotionData md2 = (MotionData) it.next();
	    data1.setQuick(i,0,(double) i);
	    data1.setQuick(i,1,md1.getData().getQuick(0,0));
	    data2.setQuick(i,0,(double) i);
	    data2.setQuick(i,1,md2.getData().getQuick(0,0));
	    i++;
	    String[] units = md1.getUnits();
	    unit = units[0];
	}
	theplot = new Plotter();
	theplot.getCurrentPlot().setYLabel(unit);
	theplot.getCurrentPlot().setMarksStyle("various");
	theplot.getCurrentPlot().setConnected(false);
	theplot.getCurrentPlot().setGrid(false);
	theplot.setYLog(true);
	theplot.addData(data1);
	theplot.addData(data2);
	
	theplot.getCurrentPlot().setTitle(title);
    }

    public void addLegend(int series, String legend) {
	theplot.getCurrentPlot().addLegend(series,legend);
    }

    public void setYLabel(String label) {
	theplot.getCurrentPlot().setYLabel(label);
    }

}

