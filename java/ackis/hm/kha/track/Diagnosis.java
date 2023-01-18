/** 
 * Tool for diagnosis of trials suspected of having artefacts.
 * Plots the raw marker data and the state estimates
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-05-05
 */

package kha.track;

import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import java.util.Hashtable;
import javax.swing.JOptionPane;

import kha.math.*;
import kha.hm.*;

public class Diagnosis {
    
    Plotter stplot = null;
    Plotter mplot = null;

    public boolean check (Trial tr) {

	if (stplot == null) 
	    stplot = new Plotter("",3);

	stplot.clear();
	stplot.setTitle(tr.toString() + "   State estimates");

	DoubleMatrix2D plotdata = tr.getData().getData();
	SeqBlas.seqBlas.dscal(180.0/Math.PI,
			      plotdata.viewPart(3,0,
						plotdata.rows()-3,
						plotdata.columns()));
	
	stplot.setCurrentPlot(0);
	stplot.addData(plotdata.viewRow(4));
	stplot.addData(plotdata.viewRow(3));
	stplot.addData(plotdata.viewRow(5));
	
	stplot.getCurrentPlot().setTitle("Rotations");
	stplot.getCurrentPlot().setYLabel("degrees");
	stplot.getCurrentPlot().addLegend(0,"About the x-axis");
	stplot.getCurrentPlot().addLegend(1,"About the y-axis");
	stplot.getCurrentPlot().addLegend(2,"About the z-axis");

	stplot.setCurrentPlot(1);
	stplot.addData(plotdata.viewRow(10));
	stplot.addData(plotdata.viewRow(9));
	stplot.addData(plotdata.viewRow(11));
	stplot.getCurrentPlot().setTitle("Rotation velocities");
	stplot.getCurrentPlot().setYLabel("degrees/s");
	stplot.getCurrentPlot().addLegend(0,"About the x-axis");
	stplot.getCurrentPlot().addLegend(1,"About the y-axis");
	stplot.getCurrentPlot().addLegend(2,"About the z-axis");


	stplot.setCurrentPlot(2);
	stplot.addData(plotdata.viewRow(16));
	stplot.addData(plotdata.viewRow(15));
	stplot.addData(plotdata.viewRow(17));
	stplot.getCurrentPlot().setTitle("Rotation accelerations");
	stplot.getCurrentPlot().setYLabel("degrees/s^2");
	stplot.getCurrentPlot().addLegend(0,"About the x-axis");
	stplot.getCurrentPlot().addLegend(1,"About the y-axis");
	stplot.getCurrentPlot().addLegend(2,"About the z-axis");


	// Marker data
	plotdata = tr.getUsedTSVData();
	String[] markers = tr.getMarkerSet();

	if (mplot == null) 
	    mplot = new Plotter(tr.toString() + "   Marker data",
				plotdata.columns()/3);
	else if (mplot.getNumOfPlots() == plotdata.columns()/3)
	    mplot.clear();
	else
	    mplot.dispose();

	mplot.setTitle(tr.toString() + "   Marker data");

	for (int i=0; i<(plotdata.columns()/3); i++) {
	    mplot.setCurrentPlot(i);

	    mplot.addData(plotdata.viewColumn(i*3));
	    mplot.addData(plotdata.viewColumn(i*3+1));
	    mplot.addData(plotdata.viewColumn(i*3+2));
	
	    mplot.getCurrentPlot().setTitle("Marker " + markers[i]);
	    mplot.getCurrentPlot().setYLabel("mm");
	}

	int svar = JOptionPane.showConfirmDialog(null,
				      "Edit the database entry for " +
				      "this trial (done later)?",
				      "Update database", 
				      JOptionPane.YES_NO_OPTION);
	if (svar == JOptionPane.YES_OPTION)
	    return false;
	else
	    return true;
    }
}

	
