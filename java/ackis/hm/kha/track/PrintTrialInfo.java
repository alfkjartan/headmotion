/** 
 * Prints info about the trial to standard out
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-05-09
 */

package kha.track;

import kha.math.*;
import kha.hm.*;

public class PrintTrialInfo {
    
    public boolean check (Trial tr) {

	System.out.println(tr);
	System.out.println(tr.getData().getTestType());

	return true;
    }
}

	
