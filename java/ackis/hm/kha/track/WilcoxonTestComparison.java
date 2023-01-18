/**
 * Performs Wilcoxon's two groups test on the differences of the two groups. 
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

public class WilcoxonTestComparison implements Comparison { 

    private Hashtable wResults = new Hashtable(3);


    public boolean compare(Group g1, Group g2, 
			   double significanceLevel ) {

	DoubleArrayList data1 = g1.getDoubleArrayList();
	DoubleArrayList data2 = g2.getDoubleArrayList();

	wilcoxon2GroupCompare(data1, data2);

	Double p = (Double) wResults.get("p");
 
	if ( (1.0 - p.doubleValue()) > significanceLevel ) {
	    return false; // H0 (groups equal) is false, reject 
	} else {
	    return true;
	}
    }	


    public Hashtable getResults() {
	return wResults;
    }

    /**  Implementation of the Wilcoxon two sample test 
	 @return  Hashtable containing the values of W+, W- and p
     */
    protected Hashtable  wilcoxon2GroupCompare(DoubleArrayList data1, 
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

	// Combine the two data sets in a matrix of three columns. The 
	// second column contains an indicator of the origin of the 
	// sample
	DoubleMatrix2D allsamples = DoubleFactory2D.dense.make(n1+n2,3);
	for (int i=0; i<n1; i++) {
	    allsamples.setQuick(i, 0, data1.getQuick(i));
	    allsamples.setQuick(i, 1, 1);
	}
	for (int i=n1; i<n1+n2; i++) {
	    allsamples.setQuick(i, 0, data2.getQuick(i-n1));
	    allsamples.setQuick(i, 1, 2);
	}

	DoubleMatrix2D sorted = Sorting.quickSort.sort(allsamples, 0);

	// Replace values by rank
	double previous = Double.MAX_VALUE;
	int start = 0;
	for (int i=0; i<n1+n2; i++) {
	    if (sorted.getQuick(i,0) == previous) { 
		double meanRank = (start + i+2) / 2.0;
		for (int j=start; j <= i; j++) {
		    sorted.setQuick(j,2, meanRank);
		    //System.out.println("meanRank=" + meanRank);
		}
	    } else {
		sorted.setQuick(i,2,i+1);
		previous = sorted.getQuick(i,0);
		start = i;
	    }
	}

	double smallestSeries=0;
	int nSmallest = 0;
	if (n1<n2) {
	    smallestSeries = 1;
	    nSmallest = n1;
	} else { 
	    smallestSeries = 2;
	    nSmallest = n2;
	}

	// Determine test parameters
	double maxW = (n1+n2)*(n1+n2+1)/2.0/2.0;
	double Wsmallest = 0;
	for (int i=0; i<n1+n2; i++) {
	    if (sorted.getQuick(i,1) == smallestSeries) {
		Wsmallest += sorted.getQuick(i,2);
	    } 
	}
	

	// Use the smallest value of W
	if (Wsmallest > maxW) {
	    Wsmallest = 2*maxW - Wsmallest;
	}


	boolean useTable = false;
	if (nSmallest<5) { //flag for use with table
	    useTable = true;
	} 
	
	
	// Compute test statistic Z
	
	double Z = (Wsmallest -0.5 -nSmallest*(n1+n2+1)/2) / 
	    Math.sqrt(n1*n2*(n1+n2+1)/12);
	
	double P = Probability.normal(Z);

	
	System.out.println("Result Wilcoxon: Wsmallest: " + Wsmallest +
			   "    p = " + 2*P +
			   "    m = " + nSmallest);
	
	if (useTable) {
	    wResults.put("Message", "Small samples => Unreliable p-value given. Look up p-value in a table instead!");
	}

	java.text.NumberFormat nf = 
	    java.text.NumberFormat.getInstance(java.util.Locale.US);
	nf.setMaximumFractionDigits(5);

	wResults.put("Wsmallest", new Double(Wsmallest));
	wResults.put("p", new Double((2*P)));
	wResults.put("n1", new Integer(n1));
	wResults.put("n2", new Integer(n2));
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

    
