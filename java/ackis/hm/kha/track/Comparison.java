/**
 * Interface for algorithms that compare two groups of data
 *
 * @author    Kjartan Halvorsen
 * @version   1.0  2003-04-08
 */

package kha.track;

import java.util.Hashtable;
import kha.hm.*;

public interface Comparison { 
    
    /**
     * Compares the two groups.
     *
     * @param g1  The first data group
     * @param g2  The second data group
     * @param significanceLevel  Value between 0 and 1, gives the 
     *            level of significance for hypothesis testing.
     *            The H0 hypothesis is always that the groups are equal. 
     * @return    true if H0 hypothesis holds, false is not.
     */
    public boolean compare(Group g1, Group g2, 
				    double significanceLevel );

    /**
     * Returns the results from the comparison
     *
     * @return    Hashtable containing test parameters and values.
     *
     */
    public Hashtable getResults();
}
    
