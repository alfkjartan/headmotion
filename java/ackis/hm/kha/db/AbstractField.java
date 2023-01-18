/** Abstract class which represents a field (column) in the database. 
 *
 * @author  Kjartan Halvorsen
 * @version 1.1 2003-04-10,  1.0 2003-02-24
 */

package kha.db;

import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import java.awt.*;


public abstract class AbstractField implements Field {

    protected Object currentValue;
    protected String heading;
    protected String name;
    protected Vector objs;
    protected boolean mutable = false;
    protected boolean searchable = false;
    protected Verifier vf;
    protected String sqlsearchstring = "";


    public AbstractField(boolean mutable, boolean searchable,
			 String heading, String name) {
	this.mutable = mutable;
	this.searchable = searchable;
	this.heading = heading;
	this.name = name;
    }

    public Component getAddGUI() {
	return null;
    }

    public Component getSearchGUI() {
	return null;
    }

    public String getName() { return this.name;}

    public String getHeading() { return this.heading;}

    public void setValue(Object o) {
	if (this.verify(o))
	    this.currentValue = o;
    }

    public Object getValue() {
	return this.currentValue;
    }

    public boolean verify() throws InvalidFieldException {
	if (this.vf != null) {
	    if (this.vf.isOK(getValue()))
		return true;
	    else {
		throw new InvalidFieldException(this.vf.getMessage());
	    }
	} else
	    return true;
    }

    public void setVerifier(Verifier v) {
	this.vf = v;
    }

    public void setNonVerifying() {
	this.vf = null;
    }

    public String getSQLSearchString(){
	return sqlsearchstring;
    }

    protected Vector queryResultSet(ResultSet rs) {
	Vector v = new Vector();

	try {
	    while (	rs.next() ) {
		v.addElement(rs.getObject(1));
	    }
	} catch (SQLException exc) {}
	
	return v;
    }

    protected boolean verify(Object o) {
	boolean ok = false;
	Object old = this.currentValue;
	this.currentValue = o;
	try {
	    ok = this.verify();
	} catch (InvalidFieldException exc) {}
	this.currentValue = old;
	return ok;
    }
	
}





	


    
