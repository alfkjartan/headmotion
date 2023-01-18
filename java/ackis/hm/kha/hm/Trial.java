/**
 * Represents a trial.
 *
 * @author   Kjartan Halvorsen
 * @version  1.1  2003-05-07    Using a Hashtable for the attributes
 *           1.0  2003-04-09
 */

package kha.hm;

import java.util.*;
import java.text.*;
import java.sql.*;
import cern.colt.matrix.*;
import cern.colt.list.*;
import cern.jet.stat.*;

import kha.db.*;
import kha.math.*;
import kha.track.*;
import kha.tsv.*;

public class Trial{

    private static DoubleFactory2D f2 = DoubleFactory2D.dense; 

    private Hashtable attributes;

    private Hashtable tsvattr = null;
    private DoubleMatrix2D tsvdata = null;
    private DoubleMatrix2D states = null;
    private MotionData md = null;

    private HeadMotionDB database = null;

    private int weight = 1; 
    // Stores number of trials that have been consolidated in this object.


    public Trial() {
	this.attributes = createAttributeHash();
    }


    /**
     * Sets an attribute of this Trial object. 
     * @param  key  the attribute key, if not contained already in the 
     *              set of attributes, the value is not set.
     * @param  val  the value to set. If not of the same class as the
     *              value already set, nothing is done.
     */
    public boolean setAttribute(String key, Object val) {
	if ((key != null) && (val != null)) {
	    if (this.attributes.containsKey(key) &&
		val.getClass().getName().equals(
			this.attributes.get(key).getClass().getName())) {
		this.attributes.put(key,val);
		return true;
	    } else 
		return false;
	} else 
	    return false;
    }

	
    /**
     * Sets the value of an attribute of this trial  given a Field object.
     * @param  field  Field object
     */
    public void setAttribute(Field field) {
	if (!setAttribute(field.getHeading(),
		     field.getValue())) {
	    // See if the field is one of tsv_attr, tsv_data, or results_data
	    try {
		if (field.getHeading().equals("tsv_attr"))
		    this.tsvattr = (Hashtable) field.getValue();
		else if (field.getHeading().equals("tsv_data"))
		    this.tsvdata = (DoubleMatrix2D) field.getValue();
	    else if (field.getHeading().equals("results_data"))
		this.states = (DoubleMatrix2D) field.getValue();
	    } catch (ClassCastException exc) {}
	}
    }

	
    /**
     * Returns an attribute of this Trial object. 
     * @param  key  the attribute key
     * @return      the value corresponding to the key, or null if key
     *              does not exist.
     */
    public Object getAttribute(String key) {
	return this.attributes.get(key);
    }
    

    public void setDatabase (HeadMotionDB db) {
	this.database = db;
    }

    public HeadMotionDB getDatabase () {
	return this.database;
    }

    public String toString() {
	return "Subject: " + this.attributes.get("subject_id") +
	    "   Test: " + this.attributes.get("test_type") 
	    + "  timestamp: " +  this.attributes.get("tsv_timestamp");
    }

    public Integer getId() {
	return (Integer) this.attributes.get("trial_ind");
    }


    public String getTestType() {
	return (String) this.attributes.get("test_type");
    }


    public Integer getSubjectId() {
	return (Integer) this.attributes.get("subject_id");
    }


    public Integer getTestOccasion() {
	return (Integer) this.attributes.get("test_occasion");
    }

    public java.sql.Timestamp getTimestamp() {
	return (java.sql.Timestamp) this.attributes.get("tsv_timestamp");
    }

    /**
     * Deletes this trial from the database.
     */
    public int removeFromDatabase() {
	if (this.database != null) {
	    return  removeFromDatabase(this.database);
	} else {
	    return -1;
	}
    }

	

    /**
     * Deletes this trial from the database.
     */
    public int removeFromDatabase(HeadMotionDB db) {
	return db.deleteTrial(this.getTimestamp());
    }

    /**
     * Returns a copy of this trial
     */
    public Trial copy() {
	Trial newtr =  new Trial();
	
	Iterator it = this.attributes.entrySet().iterator();
	while(it.hasNext()) {
	    Map.Entry pair = (Map.Entry) it.next();
	    newtr.setAttribute((String) pair.getKey(),
			       pair.getValue());
	}

	newtr.setDatabase(this.database);
	return newtr;
    }


    /**
     * Returns true if the object given is a Trial object and has
     * the same trialid.
     */
    public boolean equals(Object o) {
	return ((o instanceof Trial) &&
		(((Trial)o).getId().equals(this.getId())));
    }

    /**
     * Returns true if the Trial comes from the same subject.
     * @param  tr  Trial object to compare with.
     */
    public boolean sameSubject(Trial tr) {
	return tr.getSubjectId().equals(this.getSubjectId());
    }

    /**
     * Returns true if the Trial provided has the same subjectid and the 
     * same test type.
     * @param  tr  Trial object to compare with.
     */
    public boolean similarTo(Trial tr) {
	return ((tr.getSubjectId().equals(this.getSubjectId())) &&
		(tr.getTestType().equals(this.getTestType())));
    }

    /**
     * Sets the value of the given Field object according the 
     * attributes of this Trial object.
     * @param  field  Field object to be modified
     */
    public void setField(Field field) {
	String key = field.getHeading();
	Object val = getAttribute(key);

	if (val != null) 
	    field.setValue(val);
	else {
	    // See if the field is one of tsv_attr, tsv_data, or results_data
	    if (field.getHeading().equals("tsv_attr"))
		field.setValue(this.tsvattr);
	    else if (field.getHeading().equals("tsv_data"))
		field.setValue(this.tsvdata);
	    else if (field.getHeading().equals("results_data"))
		field.setValue(this.states);
	}
	    
    }
	


    /**
     * Applies a calculation (DataTransform) to the data
     * @param  dt  The transformation to apply
     */
    public boolean applyTransform(DataTransform dt) {
	boolean success = false;
	try {
	    this.md = dt.transform(this.getData());
	    this.tsvdata = null;
	    this.states = null;
	    success = true;
	    // TEMPORARY CODE. CHECK the smallest element. In the 
	    // first column. Write to consol if smaller than limit
	    /*
	    if (md.getData().rows()==1){
		double limit = 0.01;
		DoubleArrayList da = 
		    new DoubleArrayList(md.getData().viewColumn(0).toArray());
		if (Descriptive.min(da) < limit) {
		    System.err.println(this.attributes.get("tsv_timestamp"));
		    success = false;
		}
	    }
	    */
	} catch (MalformedDataException e) {
	    System.err.println("Bad motion data in Trial object");
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	    success = false;
	}
	return success;
    }
    

    /**
     * Alters the motiondata of this trial by the mean of this
     * and the provided trial's data.
     */
    public void consolidateMean(Trial tr) {
	MotionData mydata = getData();
	MotionData otherdata = tr.getData();

	DoubleMatrix2D myd = mydata.getData();
	DoubleMatrix2D od = otherdata.getData();
	
	boolean comparable = 
	    (mydata.getSamplingTime() == otherdata.getSamplingTime());
	comparable = (myd.rows() == od.rows());
	comparable = (myd.columns() == od.columns());

	if (comparable) {
	    myd.assign(cern.jet.math.Mult.mult(
		       ((double)weight)/((double) weight +1)));
	    myd.assign(od,
		       cern.jet.math.PlusMult.plusDiv((double) (weight + 1)));
	    weight++;
	}

	mydata.setDescription(this.toString());
    }

    
    /**
     * Returns the tsv attributes. If null, then retreive it from
     * the database.
     */
    public Hashtable getTSVAttributes() {
	if (this.md == null) {
	    loadDataFromDB();
	}
	return this.tsvattr;
    }

    /**
     * Returns the tsv marker data. If null, then retreive it from
     * the database.
     */
    public DoubleMatrix2D getTSVData() {
	if (this.md == null) {
	    loadDataFromDB();
	}
	return this.tsvdata;
    }

    /**
     * Returns the tsv marker data used in estimating the states
     */
    public DoubleMatrix2D getUsedTSVData() {
	if (this.md == null) {
	    loadDataFromDB();
	}
	TSVReader tsvr = new TSVReader(this.tsvdata,this.tsvattr);
	return tsvr.getMarkerSetData(getMarkerSet());
    }


    /**
     * Returns the data (results) for the trial. Data is not retreived from the
     * database until the first time this method is called.
     */
    public MotionData getData() {
	if (this.md == null) { 
	    loadDataFromDB();
	}
	return this.md;
    }


    /**
     * Returns the names of the set of marker used
     */
    public String[] getMarkerSet() {
	return parseMarkerSet(this.attributes.get("marker_set"));
    }


    /**
     * Frees up memory by setting the data parts of this Trial object
     * to null.
     */
    public void freeMemory() {
	this.md = null;
	this.tsvattr = null;
	this.tsvdata = null;
	this.states = null;
    }
	
    /**
     * Track the motion of the rigid body with the specified 
     * set of markers attached.
     */
    public void process() throws TrackException {

	if ((this.tsvdata == null) | (this.tsvattr == null))
	    return;
	    
	String[] markers = this.getMarkerSet();

	// Get the bandwidth parameter
	Double bw = (Double) this.attributes.get("bandwidth_param");

	String tstamp = ((java.sql.Timestamp) 
			 this.attributes.get("tsv_timestamp")).toString();
	TrackBody tb = new TrackBody(tstamp, this.tsvdata, this.tsvattr,
				     bw.doubleValue(),markers);

	int trackres = tb.track();
	tb.plot( new String[] {"x","y","z","rot x","rot y","rot z",
			       "x vel","y vel","z vel",
			       "rotvel x","rotvel y","rotvel z",
			       "x acc","y acc","z acc",
			       "rotacc x","rotacc y","rotacc z"});
	
	
	// Set the (output) data fields
	// Get the data
	this.states = tb.stateEstimates();

    }

    private void loadDataFromDB() {
	if (this.database != null) {
	    TrialData td = 
		this.database.getTrialData((java.sql.Timestamp) 
		   this.attributes.get("tsv_timestamp"));
	    this.tsvattr = td.attributes;
	    this.tsvdata = td.markerdata;
	    this.states = td.states;
	    createMotionData();
	    this.md.setDescription(this.toString());
	    this.md.setTestType((String) this.attributes.get("test_type"));
	}
    }

    private void createMotionData() {
	double samplefreq;
	try {
	    samplefreq = 
		Double.parseDouble((String) this.tsvattr.get("FREQUENCY"));
	} catch (NumberFormatException e) {
	    System.err.println("Could not find or parse FREQUENCY" +
			       " attribute of tsv file.");
	    System.err.println(e.getMessage());
	    samplefreq=240;
	}
	double samplingtime = 1.0/samplefreq;
	String[] units = {"mm","mm","mm","rad","rad","rad",
			  "mm/s","mm/s","mm/s","rad/s","rad/s","rad/s",
			  "mm/s2","mm/s2","mm/s2","rad/s2","rad/s2","rad/s2"};
	this.md = new MotionData(this.states, this.tsvattr,
				 samplingtime, units);
    }


    private boolean checkTable(Connection conn, String data_table, int subj_id,
			       String fname, DoubleMatrix2D states) {
	
	boolean ok = false;
	try {
	    Statement st = conn.createStatement();
	    ResultSet rs = st.executeQuery("SELECT results_data FROM " +
					   data_table +
					   " WHERE subject_id=" + subj_id +
					   " AND tsv_file='" + fname +"'");
	    rs.next();
	    DoubleMatrix2D thedata = (DoubleMatrix2D) rs.getObject(1);
	    return thedata.equals(states);
	} catch (SQLException exc) {
	    System.err.println("Ouch. SQLexception in checkTable.");
	    System.err.println(exc.getMessage());
	    return ok;
	}
    }
	    
    /**
     * Called from TrialGUI when  the user clicks the "Process" button. 
     * Will read the tsv file and track the motion. Depraceted.
     * @param tsvfile name of the tsv data file
     * @param markerset the set of markers to use
     * @param bandwidth  the bandwidth parameter to use
     * @param tsvattr  tsv attributes, output
     * @param tsvdata  tsv marker data, output
     * @param resultdata results, output
     */
    public static void processTrial(Field timestamp,
				    Field tsvdata, Field tsvattr,
				    Field markerset,
				    Field bandwidth, 
				    Field resultdata) 
	throws TrackException {

	String[] markers = parseMarkerSet((String) markerset.getValue());

	// Get the bandwidth parameter
	double bw = 1;
	try {
	    bw = Double.parseDouble(bandwidth.getValue().toString());
	} catch (NumberFormatException exc) {}
	
	String tstamp = ((java.sql.Timestamp) timestamp.getValue()).toString();
	TrackBody tb = new TrackBody(tstamp,
				     (DoubleMatrix2D)tsvdata.getValue(), 
				     (Hashtable) tsvattr.getValue(),
				     bw,markers);
	int trackres = tb.track();
	tb.plot( new String[] {"x","y","z","rot x","rot y","rot z",
			       "x vel","y vel","z vel",
			       "rotvel x","rotvel y","rotvel z",
			       "x acc","y acc","z acc",
			       "rotacc x","rotacc y","rotacc z"});
	
	
	// Set the (output) data fields
	// Get the data
	resultdata.setValue(tb.stateEstimates());

    }

    /**
     * Parses the timestamp value in the String, and returns
     * a java.sql.Timestamp object.
     * @param  s The string
     * @return   the timestamp as a java.sql.Timestamp object.
     */
     public static java.sql.Timestamp getTimestamp(String s) {
	 String timest = 
	     ViolinStrings.Strings.change(
					  ViolinStrings.Strings.change(
								       ViolinStrings.Strings.wordSpace(s, 1), ", ", ","), 
					  " ", ":");
	 System.err.println("TIME_STAMP " + timest);
	 SimpleDateFormat formatter
	     = new SimpleDateFormat ("yyyy-MM-dd,hh:mm:ss:S");
	 ParsePosition pos = new ParsePosition(0);
	 java.util.Date timestDate = formatter.parse(timest, pos);
	 return new java.sql.Timestamp(timestDate.getTime()); 
     }

    /**
     * Parses the timestamp value in the attributes hash, and returns
     * a java.sql.Timestamp object.
     * @param  attr  The attributes hashtable
     * @return   the timestamp as a java.sql.Timestamp object.
     */
     public static java.sql.Timestamp getTimestamp(Hashtable attr) {
	 return getTimestamp((String) attr.get("TIME_STAMP"));
     }

    /** Checks that the update was ok */

    /**
     * Tries to parse the string representation of the given object
     * into an array of strings by splitting at the commas.
     */
    private static String[] parseMarkerSet(Object o) {
	StringTokenizer tok = new StringTokenizer(o.toString(),",");
	
	int nmrks = tok.countTokens();
	String[] markers = new String[nmrks];
	for (int i=0; i<nmrks; i++) {
	    markers[i] = tok.nextToken();
	    markers[i].trim();
	}

	return markers;
    }

    private Hashtable createAttributeHash() {
	Hashtable attr = new Hashtable();
	
	attr.put("trial_ind", new Integer(-1));
	attr.put("subject_id", new Integer(-1));
	attr.put("diagnosis", new Integer(-1));
	attr.put("medication", "");
	attr.put("medication_start", java.sql.Date.valueOf("0001-01-01"));
	attr.put("phys_therapy", new Boolean(false));
	attr.put("pt_start", java.sql.Date.valueOf("0001-01-01"));
	attr.put("test_occasion", new Integer(-1));
	attr.put("test_type", "");
	attr.put("marker_set", "");
	attr.put("bandwidth_param", new Double(1.0));
	attr.put("tsv_timestamp", 
		 java.sql.Timestamp.valueOf("0001-01-01 00:00:00.0"));
	attr.put("tsv_file","");
	return attr;
    }
    
}

