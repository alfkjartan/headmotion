/**
 * Performs a matched t-test on the means in the two groups. Implements 
 * the Comparison interface.
 *
 * @author    Kjartan Halvorsen
 * @version   1.0  2003-04-29
 */

package kha.track;

import java.util.Hashtable;
import cern.colt.list.*;
import cern.jet.stat.*;

import kha.hm.*;

public class MatchedTTestComparison implements Comparison { 
    
    private Hashtable results = new Hashtable(4);

    public Hashtable getResults() { return results;}

    public boolean compare(Group g1, Group g2, 
				    double significanceLevel ) {
	double m;
	double var;

	DoubleArrayList data1 = g1.getDoubleArrayList();
	DoubleArrayList data2 = g2.getDoubleArrayList();

	int n1 = data1.size();
	int n2 = data2.size();
	int nmin = Math.min(n1,n2);

	DoubleArrayList diff = new DoubleArrayList(nmin);

	for (int i=0; i<nmin; i++) {
	    diff.setQuick(i,data1.getQuick(i)-data2.getQuick(i));
	}

	m = Descriptive.mean(diff);
	var = Descriptive.sampleVariance(diff,m);


	// Check the critical value from the t-distribution
	double tt = Probability.studentTInverse(significanceLevel,
						nmin);
	double se = Descriptive.standardError(nmin,var);

	double lowlim = m-tt*se;
	double upplim = m+tt*se;

	results.put("Mean", new Double(m));
	results.put("Standard error", new Double(se));
	results.put("Lower significance limit", new Double(lowlim));
	results.put("Upper significance limit", new Double(upplim));

	if (lowlim>0 | upplim<0) {
	    return false;
	} else {
	    return true;
	}
    }	    
}

    
