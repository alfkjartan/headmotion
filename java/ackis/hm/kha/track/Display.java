/**
 * Abstract class for 
 * objects which are able to display a set of MotionData objects
 *
 * The methods do nothing.
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-04-14
 */

package kha.track;

import java.util.Vector;

public abstract class Display {
    
    /**
     * Displays a vector of MotionData objects
     * @param  v  a Vector object which contains MotionData
     *            objects only.
     */
    public void displaySet(Vector v) {}

    /**
     * Displays a vector of MotionData objects
     * @param  v  a Vector object which contains MotionData
     *            objects only.
     * @param  title the title of the plot
     */
    public void displaySet(Vector v, String title) {}

    /**
     * Displays a single MotionData object
     * @param  d  the MotionData object to display
     */
    public void display(MotionData d) {}

    /**
     * Sets the label for the y-axis
     * @param  label the label
     */
    public void setYLabel(String label) {}

    /**
     * Sets the label for the x-axis
     * @param  label the label
     */
    public void setXLabel(String label) {}

    /**
     * Sets the title of the plot
     * @param  title the title
     */
    public void setTitle(String title) {}

    
}

