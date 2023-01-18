/** Interface for objects which represent a field (column) in the database. 
 *
 * @author  Kjartan Halvorsen
 * @version 1.0 2003-04-10 
 */

package kha.db;

import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import java.awt.*;


public interface Field {

    /**
     * Returns a gui component which enables the user to add data to the 
     * database field.
     * @return  a gui component
     */
    public Component getAddGUI();

    /**
     * Returns a gui component which enables the user to set criteria for
     * a search in the database
     * @return  a gui component, or null if the field is not be used for 
     *          searching
     */
    public Component getSearchGUI();

    /** 
     * Updates the database with the current value. Actually, the update
     * is commited at a later call to <code>st.executeUpdate()</code>
     * @param  st  a prepared statement, created by checking with the all 
     *             all the fields.
     * @param  pos the position of the column
     */
    public void writeValue(PreparedStatement st, int pos)
    throws SQLException;

    /**
     * Returns a piece of a sn SQL search string, for instance
     * " WHERE year_of_birth<1950", or an empty string if not searchable.
     */
    public String getSQLSearchString();

    /**
     * Returns the string which is used as a heading for the field in the
     * gui.
     */
    public String getName();

    /**
     * Returns the string which is used as a heading in the database
     */
    public String getHeading();

    /**
     * Returns the value set
     */
    public Object getValue();

    /**
     * Sets the value explicitly
     */
    public void setValue(Object o);

    /** 
     * Varifies that the value is valid.
     */
    public boolean verify() throws InvalidFieldException;

    /**
     * Sets the field to verify its values
     */
    public void setVerifier(Verifier v);

    /**
     * Sets the field to not verify
     */
    public void setNonVerifying();

}





	


    
