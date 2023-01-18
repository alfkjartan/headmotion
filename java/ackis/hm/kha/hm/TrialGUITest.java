/**
 * Unit tests for the TrialGUI class
 *
 * @author  Kjartan Halvorsen
 * @version 1.0  2003-04-23
 */

package kha.hm;

import junit.framework.*;
import javax.swing.*;
import java.util.Vector;
import java.awt.event.*;

import kha.db.*;

public class TrialGUITest extends TestCase {

    protected HeadMotionDB database;
    protected TrialGUI tg;

    protected TrialGUITest(String s) {
	super(s);
    }

    protected void setUp() {
	database = HeadMotionDB.getTestDB();
	tg = database.getTrialGUI();
    }

    protected void tearDown() {
	database.close();
    }

    public static Test suite() {
	TestSuite suite = new TestSuite();
	suite.addTest(new TrialGUITest("testCorrection"));
	return suite;
    }

	
    public void testCorrection() {
	Trial[] trs1 = database.searchTrials("trial_ind=2");
	tg.setFields(trs1[0]);
	String textPrior = trs1[0].toString();
	Integer idPrior = trs1[0].getId();
	String testPrior = trs1[0].getTestType();

	Vector fields = tg.getFields();
	Field testtype = (Field) fields.elementAt(7);
	Object originalValue = testtype.getValue();

	String expectedheading = "test_type";
	assertEquals(expectedheading,testtype.getHeading());

	System.err.println(testtype.getHeading());

	String newtest = "down-up";
	assertFalse(newtest.equals(testPrior));
	testtype.setValue(newtest);

	System.err.println("Firing ActionEvent");
	tg.actionPerformed(new ActionEvent(this, 0, "Correct trial"));

	System.err.println("Search database again");
	Trial[] trs2 = database.searchTrials("trial_ind=2");
	String testAfter = trs2[0].getTestType();
	assertEquals(newtest,testAfter);

	testtype.setValue(originalValue);
	tg.actionPerformed(new ActionEvent(this, 0, "Correct trial"));
	trs2 = database.searchTrials("trial_ind=2");
	String textAfter = trs2[0].toString();
	assertEquals(textPrior, textAfter);
    }
	
    public static void main (String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}

	
