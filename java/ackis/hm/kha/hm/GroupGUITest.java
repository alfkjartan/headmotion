/**
 * Unit tests for the GroupGUI class
 *
 * @author  Kjartan Halvorsen
 * @version 1.0  2003-04-13
 */

package kha.hm;

import junit.framework.*;
import javax.swing.JFrame;

public class GroupGUITest extends TestCase {

    protected HeadMotionDB database;
    protected GroupGUI groupgui;
    protected Group group0;

    protected void setUp() {

	group0 = new Group(0,"Test group 0");

	database = HeadMotionDB.getTestDB();
	TrialGUI tg = database.getTrialGUI();
	groupgui = GroupGUI.getInstance(database,tg.getFields(),
					new JFrame());
	groupgui.show(group0);
    }

    public static Test suite() {
	return new TestSuite(GroupGUITest.class);
    }

    public void testIT() {
    }
	
    public static void main (String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}

	
