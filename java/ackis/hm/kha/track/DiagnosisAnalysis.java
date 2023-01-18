/**
 * Diagnosis of a group of trials that are suspected to have artefacts.
 * Steps through each trial, plotting marker data and state estimates.
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-05-05
 */

package kha.track;

import java.util.*;
import cern.colt.matrix.*;
import kha.hm.*;

public class DiagnosisAnalysis extends  Analysis {
    
    private Diagnosis dt = new Diagnosis();

    public  Group analyze(Group g) {
	Vector  toEdit = new Vector();
	for (Iterator it = g.trials(); it.hasNext();) {
	    Trial tr = (Trial) it.next();
	    if (!dt.check(tr))
		toEdit.add(tr);
	}
	
	return new Group(-2, "Trials for editing", toEdit);
	    
    }
    
    public  void display(Group g) {
    }

    public String toString() {
	return "Diagnose";
    }

}





