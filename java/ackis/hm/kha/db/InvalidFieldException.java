/**
 * Exception which occurs if a field does not have a valid value when trying
 * to write it to the database.
 *
 * @author  Kjartan Halvorsen
 * @version 1.0 2003-04-13
 */

package kha.db;

public class InvalidFieldException extends Exception {
    public InvalidFieldException(String m) {
	super(m);
    }
}
