/**
 * Encapsulation of an int[] as an object
 *
 * @author  Kjartan Halvorsen
 * @version 1.0  2003-04-13
 *
 */

package kha.math;

import java.io.Serializable;

public class IntArray implements Serializable {

    private int[] ints;
    
    public IntArray(int[] vals) {
	this.ints = vals;
    }

    public int size() {
	return ints.length;
    }

    public int get(int ind) {
	return ints[ind];
    }

    public String toString() {
	if (ints.length == 0) return "";
	StringBuffer sb = new StringBuffer("(");
	for (int i=0; i<ints.length; i++) {
	    sb.append(ints[i]).append(",");
	}
	sb.delete(sb.length()-1,sb.length());
	sb.append(")");
	return sb.toString();
    }
}
