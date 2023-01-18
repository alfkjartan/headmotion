/**
 * Unit tests for the Trial class
 *
 * @author  Kjartan Halvorsen
 * @version 1.0  2003-04-13
 */

package kha.hm;

import junit.framework.*;
import java.sql.*;

public class TrialTest extends TestCase {

    protected HeadMotionDB database;
    protected Trial subj0t1 = null;
    protected Trial subj0t2 = null;
    protected Trial subj1t1 = null;
    protected Trial subj1t2 = null;

    protected void setUp() {

	database = HeadMotionDB.getTestDB();

	Trial[] trs = 
	    database.searchTrials("subject_id = 0");
	if (trs.length>1) {
	    subj0t1 = trs[0];
	    subj0t2 = trs[1];
	}

	trs = 
	    database.searchTrials("subject_id = 1");
	if (trs.length>1) {
	    subj1t1 = trs[0];
	    subj1t2 = trs[1];
	}
    }

    public void tearDown() {
	database.close();
    }

    public static Test suite() {
	return new TestSuite(TrialTest.class);
    }

    public void testComparison() {
	Object o = subj0t1;
	assertEquals(subj0t1,subj0t1);
	assertEquals(subj0t1,o);
	assertSame(subj0t1,o);
	assertFalse(subj1t1.equals(subj1t2));
	assertFalse(subj1t2.equals(o));
    }
	
    public static void main (String[] args) {
	junit.textui.TestRunner.run(suite());
	//FieldFactoryTest fft = new FieldFactoryTest();
	//fft.setUp();
    }

}

	
