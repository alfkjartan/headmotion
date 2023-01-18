/**
 * Sets the trialsFound list in the SearchTrial to the list of trials in the
 * given group. The user can then edit each of the trials
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-05-06
 */

package kha.hm;

import kha.track.*;

public class EditAnalysis extends  Analysis {
    
    public  Group analyze(Group g) {
	SearchTrial st = SearchTrial.getInstance();
	
	st.setTrialsFound(g.getAllTrials());
	st.showMyTab();
	return null;
    }

    
    public  void display(Group g) {
    }

    public String toString() {
	return "Edit the database entries";
    }

}





