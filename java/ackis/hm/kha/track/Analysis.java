/**
 * Base class for classes which represents a data analysis
 * and display of a Group object, or a comparison of two groups
 *
 * @author   Kjartan Halvorsen
 * @version  1.2  2003-04-29    Changed return type to Group for analyze
 *                              and compare. Added a progress bar as input
 *                              parameter.
 *           1.1  2003-04-29    Changed from interface to class
 *           1.0  2003-04-23
 */

package kha.track;

import kha.hm.*;
import javax.swing.JProgressBar;

public  class  Analysis {
    
    /**
     * Performs an analysis of a single group. 
     * @param   g  the group to analyze
     * @return  a group consisting of the trials of <code>g</code> which
     *          was not successfully analyzed. This group can be used in
     *          subsequent diagnosis.
     */
    public Group analyze(Group g){
	return null;
    }

    public void display(Group g){}

    /**
     * Compares two groups. 
     * @param   g1  the first group to analyze
     * @param   g2  the second group to analyze
     * @return  a group consisting of the trials of <code>g1</code> 
     *          and  <code>g1</code> which
     *          was not successfully analyzed. This group can be used in
     *          subsequent diagnosis.
     */
    public  Group compare(Group g1, Group g2){
	return null;
    }

    public String toString(){return "Analysis";}

}


