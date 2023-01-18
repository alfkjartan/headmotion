/** 
 * Encapsulates a set of trials. Version 1.1 uses Vector of java.sql.Timestamp
 * objects as unique trial indices.
 *
 * @author  Kjartan Halvorsen
 * @version 1.1  2003-05-02,  1.0  2003-04-09
 */

package kha.hm;

import java.util.*;
import java.sql.*;
import javax.swing.ProgressMonitor;

import cern.colt.matrix.*;
import cern.colt.list.*;

import kha.track.*;
import kha.math.*;
import kha.db.*;

public class Group {

    private Vector trialindices = null;
    private Vector trials;
    private String description;
    private int id;

    public Group(int id, String descr) {
	this.id = id;
	this.description = descr;
	this.trials = new Vector(20);
    }

    public Group(int id, String descr, Vector tr ) {
	this.id = id;
	this.description = descr;
	this.trials = tr;
    }

    public Group(int id, String descr, Vector tr, Vector trs) {
	this.id = id;
	this.description = descr;
	this.trialindices = trs;
	this.trials = new Vector(trs.size());
    }

    /**
     * Adds the trials of a given group to this group
     */
    public Group join(Group g) {
	for (Iterator it=g.trials(); it.hasNext();) 
	    this.trials.add(it.next());
	this.trialindices=null;
	return this;
    }


    public int getId() {
	return this.id;
    }



    public String getDescription() {
	return description;
    }

    public void setDescription(String d) {
	this.description = d;
    }

    public String toString() {
	return description + ",  # trials: " + trials.size();
    }

    public void addTrial(Trial t) {
	trials.add(t);
    }

    public void addTrials(Trial[] ts) {
	for (int i=0; i<ts.length; i++) {
	    addTrial(ts[i]);
	}
    }

    /**
     * Loads the trials from a database, by searching for trials with
     * indices equal to trialindices.
     * @param  db  The HeadMotionDB database
     */
    public void loadTrials(HeadMotionDB db) {
	if (trialindices != null) {
	    if (trialindices.size()>0) {
		StringBuffer buf = new StringBuffer("tsv_timestamp IN (");
		for (Iterator it = trialindices.iterator(); it.hasNext();) 
		    buf.append("'" +
			       ((java.sql.Timestamp) it.next()).toString() + 
			       "',");
		buf.delete(buf.length()-1,buf.length());
		buf.append(")");

		Trial[] trs =  db.searchTrials(buf.toString());
		addTrials(trs);
	    }
	}
    }

		

    /**
     * Frees up memory by setting the data parts of each Trial object
     * to null.
     */
    public void freeMemory() {
	for (Iterator it = trials.iterator(); it.hasNext();) {
	    ((Trial) it.next()).freeMemory();
	}
    }


    public Vector getAllTrials() {
	return trials;
    }

    public Iterator trials() {
	return getAllTrials().iterator();
    }

    public void clearAllTrials() {
	getAllTrials().clear();
    }


    public Vector getTrialIndices() {
	if (trialindices == null) {
	    int size = trials.size();
	    trialindices = new Vector(size);
	    for (int i=0; i<size; i++) 
		trialindices.add(((Trial) trials.elementAt(i)).getTimestamp());
	}
	return trialindices;
    }
    

    public int size() {
	return trials.size();
    }


    public Group copy() {
	Vector newTrials = new Vector(this.trials.size());
	for (Iterator it = this.trials.iterator(); it.hasNext();) 
	    newTrials.add(((Trial) it.next()).copy());

	return new Group(this.id, this.description, newTrials);
    }

    public void updateDatabase(DB db) throws SQLException {
	// Check first if the group already exists in the database
	ResultSet rs = db.inquire("SELECT * FROM " +
				  HeadMotionDB.GROUP_TABLE +
				  " WHERE group_id=" +
				  this.id);
	String sqls;
	if (rs.next()) {
	    rs.close();
	    sqls = "UPDATE " + HeadMotionDB.GROUP_TABLE
		+ " SET description=?, trial_indices=? "
		+ "WHERE group_id=" 
		+ this.id;
	    PreparedStatement ps = 
		db.getConnection().prepareStatement(sqls);
	    ps.setString(1,this.description);
	    ps.setObject(2,this.getTrialIndices());
	    ps.executeUpdate();
	} else { // new group
	    rs.close();
	    sqls = "INSERT INTO " + HeadMotionDB.GROUP_TABLE
		+ " (group_id, description, trial_indices) "
		+ "VALUES (?,?,?)";
	    PreparedStatement ps = 
		db.getConnection().prepareStatement(sqls);
	    // TEMPRORARY CODE
	    System.err.println("DEBUG GROUP.JAVA");
	    System.err.println("id: " + this.id);
	    System.err.println(this.description);
	    System.err.println("trial_ind: " + this.getTrialIndices());
	    
	    ps.setInt(1,this.id);
	    ps.setString(2,this.description);
	    ps.setObject(3,this.getTrialIndices());
	    ps.executeUpdate();
	}
    }

    /**
     * Maps the given DataTransform to each Trial in the group.
     * @param    dt  the transformation
     * @return   A new Group object containing the trials that was
     *           not successfully processed. This group can be used
     *           for subsequent diagnostics.
     */
    public Group map(DataTransform dt) {
	return map(dt,null,0);
    }

    /**
     * Maps the given DataTransform to each Trial in the group.
     * @param    dt  the transformation
     * @param    pgm progress monitor
     * @param    prgr the current progress
     * @return   A new Group object containing the trials that was
     *           not successfully processed. This group can be used
     *           for subsequent diagnostics.
     */
    public Group map(DataTransform dt, ProgressMonitor pgm,
		     int prgr) {
	Vector badtrials = new Vector();
	Enumeration en = trials.elements();
	int i=0;
	while (en.hasMoreElements()) {
	    Trial tr = (Trial) en.nextElement();
	    if (!tr.applyTransform(dt)) {
		badtrials.add(tr);
	    }
	    if (pgm != null) {
		pgm.setProgress(prgr+i);
		i++;
		if (pgm.isCanceled()) {
		    return null;
		}
	    }
	}
	return new Group(-1, this.description + "Unsuccessful trials",
			 badtrials);
    }


    /** 
     * Displays the data contained in the group
     */
    public void display(Display d) {
	Vector vec = new Vector();
	for (Enumeration en = trials.elements();
	     en.hasMoreElements();) {
	    vec.add(((Trial) en.nextElement()).getData());
	}
	d.displaySet(vec);
    }

    /**
     * Finds trials from same subject with same test type, and reduces
     * the set of trials by replacing the similar trials with the mean. 
     */
    public void consolidate() {
	int ind = 0;

	while(ind < trials.size()) {
	    Trial tr = (Trial) trials.elementAt(ind);
	    
	    int ln = trials.size();
	    for (Iterator it2 = 
		     trials.subList(ind+1, ln).iterator(); 
		 it2.hasNext();) {
		Trial tr2 = (Trial) it2.next();

		if (tr.similarTo(tr2)) {
		    tr.consolidateMean(tr2);
		    it2.remove();
		}
	    }
	    ind++;
	}
    }

    /**
     * Finds a matching trial from the given group, and sorts the own
     * groups accordingly. OBS. trials that find no match between the two
     * are removed from the groups.
     * @param gr   the group to compare with
     */
    public void findMatch(Group gr) {
	Vector nv = new Vector();
	
	for (Iterator other = gr.trials(); other.hasNext();) {
	    Trial otr = (Trial) other.next();
	    boolean foundmatch = false;
	    for (Iterator my = this.trials(); my.hasNext();) {
		Trial mytr = (Trial) my.next();
		if (mytr.similarTo(otr)) {
		    nv.add(mytr);
		    foundmatch = true;
		    break;
		}
	    }
	    if (!foundmatch) 
		other.remove();
	}

	this.trials = nv;
    }


    /**
     * Returns DoubleArrayList object, where each Trial provides
     * an element. Only meaningful if a DataTransform has changed the
     * data of each trial to be scalar valued.
     */
    public DoubleArrayList getDoubleArrayList() {
	DoubleArrayList l = new DoubleArrayList(trials.size());
	for (Iterator it = this.trials(); it.hasNext();) {
	    Trial tr = (Trial) it.next();
	    DoubleMatrix2D data = tr.getData().getData();
	    l.add(data.get(0,0));
	}
	return l;
    }

}

