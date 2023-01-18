/**
 * Factory for objects implementing the Verifyer interface. The objects
 * are used to verify that values of Field objects are ok.
 *
 * @author  Kjartan Halvorsen
 * @version 1.0  2003-04-10
 */

package kha.db;

import java.io.*;

public class VerifierFactory {
    
    public static Verifier nonEmptyString() {
	return new NonEmptyStringVerifier();
    }
    
    public static Verifier nonEmptyString(String message) {
	return new NonEmptyStringVerifier(message);
    }

    public static Verifier isFloat() {
	return new IsFloatVerifier();
    }

    public static Verifier isInt() {
	return new IsIntVerifier();
    }

    public static Verifier isFile() {
	return new IsFileVerifier();
    }

    public static Verifier alwaysOK() {
	return new BaseVerifier();
    }
}

class BaseVerifier implements Verifier {

    protected String message = "";

    
    public BaseVerifier() {
	this.message = "";
    }
    
    public BaseVerifier(String message) {
	this.message = message;
    }

    public boolean isOK (Object o) { return true;}
    
    public String getMessage() {
	return message;
    }
}
			     


class NonEmptyStringVerifier extends BaseVerifier {
    
    public NonEmptyStringVerifier(String message) {
	super(message);
    }
	
    public NonEmptyStringVerifier() {
	super("Empty string");
    }
	
    public boolean isOK (Object o) {
	if (o.toString().length() == 0) {
	    return false;
	} else 
	    return true;
    }
}

class IsFloatVerifier extends BaseVerifier {

    public boolean isOK (Object o) {
	boolean ok = false;
	try {
	    Double dd = new Double(o.toString());
	    ok = true;
	} catch (NumberFormatException e) {
	    message = "Value is not a float";
	}
	return ok;
    }
}

class IsIntVerifier extends BaseVerifier {

    public boolean isOK (Object o) {
	boolean ok = false;
	try {
	    Integer ii = new Integer(o.toString());
	    ok = true;
	} catch (NumberFormatException e) {
	    message = "Value is not an integer";
	    System.err.println("Object not representing an integer value");
	}
	return ok;
    }
}

class IsFileVerifier extends BaseVerifier {

    public boolean isOK (Object o) {
	try {
	    File fil = new File(o.toString());
	    return fil.exists();
	} catch (Exception ee) {
	    return false;
	}
    }
}

