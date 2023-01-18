/**
 * Prints info about each trial in the group
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-05-09
 */

package kha.track;

import java.util.*;
import kha.hm.*;

public class PrintInfoAnalysis extends  Analysis {
    
    private PrintTrialInfo dt = new PrintTrialInfo();

    public  Group analyze(Group g) {
	for (Iterator it = g.trials(); it.hasNext();) {
	    dt.check((Trial) it.next());
	}
	
	return null;
	    
    }
    
    public  void display(Group g) {
    }

    public String toString() {
	return "Print info";
    }

}





