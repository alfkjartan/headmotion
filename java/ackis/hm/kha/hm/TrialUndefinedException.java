/**
 * Exception which occurs if a Trial object cannot be instnatiated.
 *
 * @author  Kjartan Halvorsen
 * @version 1.0 2003-04-13
 */

package kha.hm;

public class TrialUndefinedException extends Exception {
    public TrialUndefinedException(String m) {
	super(m);
    }
}
