/**
 * Performs Wilcoxon's signed rank test on the means in the two groups. 
 * Implements the Comparison interface.
 *
 * @author    Kjartan Halvorsen
 * @version   1.0  2003-08-15
 */

package kha.track;

import java.util.Hashtable;
import cern.colt.matrix.*;
import cern.colt.matrix.doublealgo.Sorting;
import cern.colt.list.*;
import cern.jet.stat.*;

import kha.hm.*;

public class WilcoxonSRTestComparison implements Comparison { 

    private Hashtable wResults = new Hashtable(3);


    public boolean compare(Group g1, Group g2, 
				    double significanceLevel ) {

	DoubleArrayList data1 = g1.getDoubleArrayList();
	DoubleArrayList data2 = g2.getDoubleArrayList();

	wilcoxonCompare(data1, data2);

	Double p = (Double) wResults.get("p");
 
	if ( (1.0 - p.doubleValue()) > significanceLevel ) {
	    return false; // H0 (groups equal) is false, reject;
	} else {
	    return true;
	}
    }	


    public Hashtable getResults() {
	return wResults;
    }

    /**  Implementation of the Wilcoxon matched-samples signed rank test 
	 @return  Hashtable containing the values of W+, W- and p
     */
    protected Hashtable  wilcoxonCompare(DoubleArrayList data1, 
				    DoubleArrayList data2) {
	int n1 = data1.size();
	int n2 = data2.size();
	int nmin = Math.min(n1,n2);

	// Compute the mean and range for the two sets.
	DoubleArrayList data1sorted = data1.copy();
	data1sorted.sort();
	DoubleArrayList data2sorted = data2.copy();
	data2sorted.sort();
	
	double median1 = Descriptive.median(data1sorted);
	double median2 = Descriptive.median(data2sorted);

	double max1 = data1sorted.getQuick(n1-1);
	double min1 = data1sorted.getQuick(0);
	double max2 = data2sorted.getQuick(n2-1);
	double min2 = data2sorted.getQuick(0);
       
	double mean1 = Descriptive.mean(data1sorted);
	double mean2 = Descriptive.mean(data2sorted);

	double stdv1 = 
	    Math.sqrt(Descriptive.sampleVariance(data1sorted, mean1));
	double stdv2 = 
	    Math.sqrt(Descriptive.sampleVariance(data2sorted, mean2));

	// absdiff will hold the differences, the absolute differences
	// and the ranks
	DoubleMatrix2D absdiff = DoubleFactory2D.dense.make(nmin,3);
	for (int i=0; i<nmin; i++) {
	    absdiff.setQuick(i,0,
			     data1.getQuick(i)-data2.getQuick(i));
	    absdiff.setQuick(i,1,
			     Math.abs(absdiff.getQuick(i,0)));
	}

	DoubleMatrix2D firstsortdiff = Sorting.quickSort.sort(absdiff,1);

	//Exclude zero differences
	DoubleMatrix2D sortdiff = 
	    firstsortdiff.viewSelection( new  DoubleMatrix1DProcedure() {
		    public boolean apply(DoubleMatrix1D element) {
			return (element.get(0) != 0);
		    }});

	nmin = sortdiff.rows();

	// Replace difference by rank
	double previous = Double.MAX_VALUE;
	int start = 0;
	for (int i=0; i<nmin; i++) {
	    if (sortdiff.getQuick(i,1) == previous) { 
		double meanRank = (start + i+2) / 2.0;
		for (int j=start; j <= i; j++) {
		    sortdiff.setQuick(j,2, meanRank);
		    //System.out.println("meanRank=" + meanRank);
		}
	    } else {
		sortdiff.setQuick(i,2,i+1);
		previous = sortdiff.getQuick(i,1);
		start = i;
	    }
	}
		
	
	// Determine test parameters
	
	double Wpluss = 0;
	double Wmin = 0;
	for (int i=0; i<nmin; i++) {
	    if (sortdiff.getQuick(i,0) > 0) {
		Wpluss += sortdiff.getQuick(i,2);
	    } else {
		Wmin += sortdiff.getQuick(i,2);
	    }
	    //
	}
	
	// Compute test statistic Z

	double W = Math.max(Wpluss, Wmin);
	double Z = (W -0.5 -nmin*(nmin+1)/4) / 
	    Math.sqrt(nmin*(nmin+1)*(2*nmin+1)/24);

	double P = Probability.normal(Z);

	
	System.out.println("Result Wilcoxon: W+: " + Wpluss +
			   "    W-: " + Wmin +
			   "    p = " + 2*(1-P) +
			   "    N = " + nmin);
	
	java.text.NumberFormat nf = 
	    java.text.NumberFormat.getInstance(java.util.Locale.US);
	nf.setMaximumFractionDigits(5);

	wResults.put("W+", new Double(Wpluss));
	wResults.put("W-", new Double(Wmin));
	wResults.put("p", new Double((2*(1-P))));
	wResults.put("N", new Integer(nmin));
	wResults.put("Mean, Standard dev, Min, Max and Median, Group 2",
		     " (" + nf.format(mean2) + ", " + nf.format(stdv2) + 
		     ", " + nf.format(min2) + ", " + nf.format(max2) + 
		     ", " + nf.format(median2) + ")");
	wResults.put("Mean, Standard dev, Min, Max and Median, Group 1",
		     " (" + nf.format(mean1) + ", " + nf.format(stdv1) + 
		     ", " + nf.format(min1) + ", " + nf.format(max1) + 
		     ", " + nf.format(median1) + ")");


	return wResults;
    }	    
}

    
