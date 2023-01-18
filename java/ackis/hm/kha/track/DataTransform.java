/**
 * Interface for algorithms that transform data into another format which
 * facilitates analysis.
 *
 * @author    Kjartan Halvorsen
 * @version   1.0  2003-04-08
 */

package kha.track;

public interface DataTransform { 
    
    /**
     * transforms the MotionData object. Returns another MotionData object
     *
     * @param md  input data
     * @return    transformed motion data
     */
    public MotionData transform(MotionData md) throws MalformedDataException;

    /**
     * Gives a short description of the transform
     */
    public String toString();

}
    
