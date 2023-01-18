/**
 * Superclass for objects  encapsulating databases. The class enforces 
 * Singleton 
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-04-10
 */

package kha.db;

import java.util.*;
import java.sql.*;

public class DB {

    protected String db_url;
    protected String user;
    protected String passwd;
    protected Connection conn;
    protected Statement stmt;

    protected DB (String url, String user, String passwd) {
	
	this.db_url = url;
	this.user = user;
	this.passwd = passwd;

        // Load the HSQL Database Engine JDBC driver
        // hsqldb.jar should be in the class path or made 
	// part of the current jar
	this.conn=connect(db_url,user,passwd);
    }


    /**
     * Tries to open a connection to the database
     *
     * @param  db_url  String with url to the database
     * @param  user    the user name
     * @param  passwd  the password
     * @return         An object implementing java.sql.Connection
     */
    protected Connection  connect(String db_url, String user, String passwd) {
	Connection con = null;
	try {
	    Class.forName("org.hsqldb.jdbcDriver");
	    
	    // connect to the database. 
	    try {
		con = DriverManager.getConnection(db_url,
						   user,
						   passwd);
	    } catch (SQLException exc) {
		System.err.println("Unable to connect to database!");
		System.err.println(exc.getMessage());
		System.err.println("url: " + db_url);
		System.err.println("user: " + user);
		System.err.println("pwd: " + passwd);
		System.exit(1);
	    }
	} catch (ClassNotFoundException exc) {
	    System.err.println("Database driver not found!");
	    System.exit(1);
	}
	return con;
    }

    public Connection getConnection() {
	return this.conn;
    }

    /** 
     * Creates the tables if not already existing
     * @param c  the connection, established with a call to 
     *           <code>connect</code>.
     * @param query the SQL String which will create the table
     */
    protected void createTable(String query) {
	try {
	    this.stmt =  this.conn.createStatement();            
	    stmt.executeQuery(query);
	    System.out.println("Table created");
	} catch (SQLException e) {
	    // Ended up here because the table exists
	    //System.out.println("Table not created. Probably existing.");
	    System.out.println(e.getMessage());
	}
    }
    

    /**
     * Accessor
     d()) 
		this.conn=connect(this.db_url,
				  this.user,
				  this.passwd);
	} catch (SQLException exc) {}
	return this.conn;
    }


    /**
     * Simple SQL query
     *
     * @param  query  the query to perform
     * @return        A result set, or null if SQLxception occurred
     */
    public ResultSet inquire(String query) {
	if (this.stmt == null) {
	    try {
		this.stmt = this.conn.createStatement();
	    } catch (SQLException exc) {
		System.err.println("Unable to create statement");
		exc.printStackTrace();
		return null;
	    }
	}

	try {
	    return this.stmt.executeQuery(query);
	} catch (SQLException exc) {
		System.err.println("Exception when executing query:");
		System.err.println(query);
		return null;
	}
    }


    
    /**
     * SQL query returning a scrollable and updateable ResultSet
     *
     * @param  query  the query to perform
     * @return        A result set, or null if SQLException occurred
     */
    /*
    public ResultSet inquireUpdateable(String query) {
	try {
	    this.stmt = 
		this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					  ResultSet.CONCUR_UPDATEABLE);
	    return inquire(query);
	} catch (SQLException exc) {
	    System.err.println("Unable to create statement");
	    exc.printStackTrace();
	    return null;
	}
    }
    */

    
    /**
     * SQL INSERT, UPDATE or DELETE statements
     *
     * @param  cmnd  the statement to execute
     * @return       row count
     */
    public int execute(String cmnd) {
	if (this.stmt == null) {
	    try {
		this.stmt = this.conn.createStatement();
	    } catch (SQLException exc) {
		System.err.println("Unable to create statement");
		exc.printStackTrace();
		return -1;
	    }
	}

	try {
	    return this.stmt.executeUpdate(cmnd);
	} catch (SQLException exc) {
		System.err.println("Exception when executing command:");
		System.err.println(cmnd);
		return -2;
	}
    }

    /**
     * Adds an entry to a table.
     *
     * @param  tname    the name of the table
     * @param  fields   a Vector of Field objects
     */
    public void updateTable(String tname, Vector fields) throws SQLException { 
	insertRow("INSERT INTO " + tname, fields);
    }

    /**
     * Does the job.
     */
    private void insertRow(String cmnd, Vector fields) 
	throws SQLException { 

	// Form the SQL statement
	StringBuffer sqls = new StringBuffer(cmnd  + " (");
	for (Enumeration en = fields.elements(); en.hasMoreElements();) {
	    sqls.append(((Field)en.nextElement()).getHeading());
	    sqls.append(", ");
	}
	sqls.delete(sqls.length()-2,sqls.length());
	sqls.append(") VALUES (");
	for (int i=0; i<fields.size(); i++) {
	    sqls.append("?,");
	}
	sqls.deleteCharAt(sqls.length()-1);
	sqls.append(")");
	
	PreparedStatement ps = this.conn.prepareStatement(sqls.toString());

	// Let the fields set their values
	for (int i=0; i<fields.size(); i++) {
	    ((Field) fields.elementAt(i)).writeValue(ps,(i+1));
	}

	//Commit
	int i = -1;
	i=ps.executeUpdate();
        if (i == -1) {
            System.out.println("db error : " + sqls);
        }

        try {
	    ps.close();
	} catch (SQLException exc) {}

    }

    /**
     * Updates an existing row
     *
     * @param  tname    the name of the table
     * @param  fields   a Vector of Field objects
     * @param  qualif   the field used as qualifier
     */
    public void updateTable(String tname, Vector fields, Field qualif) 
	throws SQLException { 
	// Form the SQL statement
	StringBuffer sqls = new StringBuffer("UPDATE " + tname + " SET ");
	for (Enumeration en = fields.elements(); en.hasMoreElements();) {
	    Field f = (Field) en.nextElement();
	    if (!(f.equals(qualif))) {
		sqls.append(f.getHeading());
		sqls.append("=?, ");
	    }
	}
	sqls.delete(sqls.length()-2,sqls.length());
	sqls.append(" WHERE ");
	sqls.append(qualif.getHeading());
	sqls.append("=?");
	
	System.err.println(sqls);

	PreparedStatement ps = this.conn.prepareStatement(sqls.toString());

	// Let the fields set their values
	int i=0;
	for (Enumeration en = fields.elements(); en.hasMoreElements();) {
	    Field f = (Field) en.nextElement();
	    if (!(f.equals(qualif))) {
		System.err.println(f.getHeading() + " qmark: " + (i+1));
		f.writeValue(ps,(i+1));
		i++;
	    }
	}
	qualif.writeValue(ps,(i+1));

	//Commit
	i = -1;
	i=ps.executeUpdate();
        if (i == -1) {
            System.out.println("db error : " + sqls);
        }

        try {
	    ps.close();
	} catch (SQLException exc) {}

    }

    /**
     * Returns the number of rows in the database
     */
    public int rows(String table) throws SQLException {
	ResultSet rs = inquire("SELECT COUNT(*) FROM " + table);
	rs.next();
	return rs.getInt(1);
    }

    /** 
     * Closes the database
     */
    public void close() {
	close(false);
    }

    public void close(boolean compact) {
	System.err.println("Shutting down database");
	if (compact)
	    inquire("SHUTDOWN COMPACT");
	else
	    inquire("SHUTDOWN");
	try {
	    this.conn.close();
	} catch (SQLException exc) {
	    System.err.println("Could not cleanly close database");
	    System.err.println(exc.getMessage());
	}
    }

}



    
