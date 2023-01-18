/**
 * Computes the scaled work for the trials in the group, reduces the set
 * of trials by computing the mean over similar trials (similar trials are
 * trials with identical subject_id and test_type fields). Finally, the
 * class displays the result, grouped by test_type.
 * Implements Analysis
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-04-23
 */
 
package kha.track;

import java.util.Hashtable;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import cern.colt.matrix.*;
import kha.hm.*;

public class ScaledWorkRetestAnalysis extends  Analysis {
    
    private double significance = 0.95;
    private DataTransform dt = new AbsoluteScaledWork();
    private PlotPairsDisplay displ = new PlotPairsDisplay();
    //private Comparison ttest = new MatchedTTestComparison();
    private Comparison ttest = new WilcoxonSRTestComparison();


    public Group compare(Group g1, Group g2){
	
	ProgressMonitor pgm = 
	    new ProgressMonitor(AnalyzeGUI.getInstance(),
				this.toString(),
				"Processing group:  " + g1.getDescription(), 
				0, g1.size() + g2.size());

	pgm.setProgress(0);

	Group bad1 = g1.map(dt,pgm,0);
	g1.consolidate();
	
	if (pgm.isCanceled()) 
	    return null;
	pgm.setNote("Processing group:  " + g2.getDescription());
	Group bad2 = g2.map(dt,pgm,g1.size());
	g2.consolidate();
	
	if (pgm.isCanceled()) 
	    return null;
	pgm.close();


	g1.findMatch(g2);
	boolean res = ttest.compare(g1,g2,significance);
	Hashtable resHash = ttest.getResults();
	StringBuffer resstr = new StringBuffer();
	for (Enumeration keys=resHash.keys(); keys.hasMoreElements();) {
	    String key = (String) keys.nextElement();
	    resstr.append(key).append("=").append(resHash.get(key));
	    resstr.append("\n");
	}

	JOptionPane.showMessageDialog(null, "Testing the difference of two" +
				      "groups\n" + 
				      "Group 1: " + g1.toString() + "\nand\n" +
				      "Group 2: " + g2.toString() + "\n" +
				      "Using the Wilcoxon matched-pairs " +
				      "signed-rank test.\n\n" + 
				      "Keeping the H0 (groups equal) " + 
				      " hypothesis: " + res +
				      "\nResults:\n" + resstr.toString());
	

	Vector data = new Vector(2*g1.size());
	Iterator it1 = g1.trials();
	for (Iterator it2=g2.trials(); it2.hasNext(); ) {
	    data.add(((Trial) it1.next()).getData());
	    data.add(((Trial) it2.next()).getData());
	}
	displ.displaySet(data, this.toString());
	
	displ.addLegend(0,g1.getDescription());
	displ.addLegend(1,g2.getDescription());
	displ.setYLabel("eei");
	
	
	bad1.join(bad2);
	bad1.setDescription(toString() + ", bad trials. Groups:   " +
			    g1.getDescription() + ",  " +
			    g2.getDescription());
	return bad1;
    }

    public String toString() {
	return "Energy expenditure test-retest";
    }

}





