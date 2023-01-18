/**
 * Computes brief statistical description of the data set
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2004-04-08
 */

package kha.track;

import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import cern.colt.matrix.*;
import cern.colt.matrix.doublealgo.Sorting;
import cern.colt.list.*;
import cern.jet.stat.*;
import kha.hm.*;

public class BriefStatDescription {

    public Hashtable describe(Group g) {

	DoubleArrayList data1 = g.getDoubleArrayList();

	int n1 = data1.size();

	// Compute the mean and range for the two sets.
	DoubleArrayList data1sorted = data1.copy();
	data1sorted.sort();
	
	double median1 = Descriptive.median(data1sorted);

	double max1 = data1sorted.getQuick(n1-1);
	double min1 = data1sorted.getQuick(0);

	double mean1 = Descriptive.mean(data1sorted);

	double stdv1 = 
	    Math.sqrt(Descriptive.sampleVariance(data1sorted, mean1));

	java.text.NumberFormat nf = 
	    java.text.NumberFormat.getInstance(java.util.Locale.US);
	nf.setMaximumFractionDigits(5);

	Hashtable wResults = new Hashtable();

	wResults.put("Mean", nf.format(mean1));
	wResults.put("Standar deviation", nf.format(stdv1));
	wResults.put("Median", nf.format(median1));
	wResults.put("Minimum", nf.format(min1));
	wResults.put("Maximum", nf.format(max1));

	return wResults;
    }
}
