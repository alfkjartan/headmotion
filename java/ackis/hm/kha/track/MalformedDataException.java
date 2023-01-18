/**
 * Exception which occurs if a DataTransform object receives a bad 
 * MotionData object
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-04-14
 */

package kha.track;

public class MalformedDataException extends Exception {
    public MalformedDataException(String m) {
	super(m);
    }
}
