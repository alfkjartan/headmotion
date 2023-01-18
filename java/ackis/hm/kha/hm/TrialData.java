/** Simple struct to hold tsvdata, attributes and estimated states.
 *
 * @author  Kjartan Halvorsen
 * @version 1.0 2003-04-22
 */

package kha.hm;

import java.util.*;
import cern.colt.matrix.*;

public class TrialData {

    public Hashtable attributes;
    public DoubleMatrix2D markerdata;
    public DoubleMatrix2D states;

    public TrialData(Hashtable at, DoubleMatrix2D md, DoubleMatrix2D st) {
	attributes = at;
	markerdata = md;
	states = st;
    }
}
