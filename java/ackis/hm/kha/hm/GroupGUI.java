/** 
 * GUI that lets the user add and remove trials to and from a group.
 * Singleton.
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-04-11
 */

package kha.hm;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import kha.db.*;
import kha.track.*;

public class GroupGUI extends JDialog implements ActionListener {

    private static GroupGUI theGUI = null;

    private HeadMotionDB database;

    private Vector trialfields;
    private Group currentGroup;

    private Vector trialsChosen;
    private Vector trialsFound;
    private JList tcList;
    private JList tfList;

    private GroupGUI(HeadMotionDB database, Vector tf, 
		     Frame parent) {
	super(parent,true);
	
	this.database = database;
	this.trialfields = tf;

	this.trialsChosen = new Vector();
	this.trialsFound = new Vector();

	createGUI();
	this.hide();
    }

    public static GroupGUI getInstance(HeadMotionDB database,
				       Vector tf, Frame parent) {
	if (theGUI == null) {
	    theGUI = new GroupGUI(database, tf, parent);
	}
	return theGUI;
    }


    /* Implementation of the ActionListener interface */
    public void actionPerformed(ActionEvent e) {
	// The user is done
	try {
	    this.currentGroup.updateDatabase(this.database);
	} catch (SQLException exc) {
	    System.err.println("SQLException");
	    System.err.println(exc.getMessage());
	    exc.printStackTrace();
	}
	hide();
    }

    /**
     * Override the <code>show()</code> method so the only way
     * to show the dialog is to supply a Group object.
     */
    public void show() {}

    public void show(Group g) {
	this.currentGroup = g;
	super.setTitle(g.getDescription());
	this.trialsChosen = g.getAllTrials();
	this.tcList.setListData(this.trialsChosen);
	this.trialsFound.clear();
	this.tfList.setListData(this.trialsFound);
	super.show();
    }

    /** 
     * Closing, or more precisely, hiding the dialog
     */
    public void close() {
	database.close();
	dispose();
    }


    /**
     * The actionPerformed when pressing the "Search" button ends
     * up here.
     */
    private void searchTrials() {
	StringBuffer sqlstr = new StringBuffer();
	for (Enumeration en = trialfields.elements(); 
	     en.hasMoreElements();) {
	    Field f = (Field) en.nextElement();
	    String sstr = f.getSQLSearchString();
	    if (sstr.length()>2) {
		sqlstr.append(sstr);
		sqlstr.append(" AND ");
	    }
	}
	
	if (sqlstr.length()>4) {
	    sqlstr.delete(sqlstr.length()-5,sqlstr.length());
	}

	System.err.println(sqlstr);
	
	Trial[] foundtrials = 
	    this.database.searchTrials(sqlstr.toString());

	System.err.println("found trials: " + foundtrials.length);
	
	// add the results to the list of trials found
	for (int i=0; i<foundtrials.length; i++) {
	    addIfUnique(this.trialsFound,foundtrials[i]);
	}
	this.tfList.setListData(this.trialsFound);
    }

    /** 
     *The actionPerformed when pressing the "Add>>" button ends up here.
     */
    private void addSelectedTrials(){
	Object[] selected = this.tfList.getSelectedValues();
	for (int i=0; i<selected.length; i++) {
	    addIfUnique(this.trialsChosen, (Trial) selected[i]);
	}
	this.tcList.setListData(this.trialsChosen);
    }


    /** 
     *The actionPerformed when pressing the "Remove" button ends up here.
     */
    private void removeSelectedTrials(){
	int[] selected = this.tcList.getSelectedIndices();
	for (int i=selected.length-1; i>-1; i--) {
	    this.trialsChosen.remove(selected[i]);
	}
	this.tcList.setListData(this.trialsChosen);
    }


    private void addIfUnique(Vector vec, Trial trial) {
	boolean unique = true;
	if (trial == null) return;
	for (Enumeration en = vec.elements(); 
	     en.hasMoreElements();) {
	    if (trial.equals(en.nextElement())) {
		unique = false;
		break;
	    }
	}
	if (unique)
	    vec.add(trial);
    }
	

    private void createGUI() {
	getContentPane().setLayout(new BorderLayout());

	// Make the gui for setting searc criteria
	JPanel fieldp = new JPanel();
	GridBagLayout gridb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	fieldp.setLayout(gridb);
	
	int row = 0;
	gbc.weightx = 0.5;
	gbc.fill = GridBagConstraints.HORIZONTAL; 

	for (Enumeration e = trialfields.elements(); e.hasMoreElements();) {
	    Field thefield = (Field) e.nextElement(); 
	    Component fgui = thefield.getSearchGUI();
	    if (fgui != null) {
		JLabel descr = new JLabel(thefield.getName(),JLabel.TRAILING);

		gbc.gridy = row;
		gbc.gridx = 0;
		gridb.setConstraints(descr,gbc);
		fieldp.add(descr);

		gbc.gridx = 1;
		gridb.setConstraints(fgui,gbc);
		fieldp.add(fgui);
		row++;
	    }
	}

	fieldp.setBorder(
			 BorderFactory.createCompoundBorder(
			    BorderFactory.createTitledBorder("Search trials"),
	       BorderFactory.createEmptyBorder(6,3,6,3)));


	// Make buttons
	JPanel btnp = new JPanel();
	btnp.setLayout(new BoxLayout(btnp, BoxLayout.X_AXIS));
	JButton srchbtn = new JButton("Search for trials");
	srchbtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    searchTrials();
		}
	    });
	btnp.add(srchbtn);
	btnp.add(Box.createHorizontalGlue());
	JButton addbtn = new JButton("Add trials");
	addbtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    addSelectedTrials();
		}
	    });
	btnp.add(addbtn);
	btnp.add(Box.createRigidArea(new Dimension(5, 0)));
	JButton delbtn = new JButton("Remove trials");
	delbtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    removeSelectedTrials();
		}
	    });
	btnp.add(delbtn);

	JPanel jp1 = new JPanel();
	jp1.setLayout(new BorderLayout());
	jp1.add(fieldp,BorderLayout.CENTER);
	jp1.add(btnp,BorderLayout.SOUTH);

	
	// Found trials scroll pane
	this.tfList = new JList();
	JScrollPane tfpane = new JScrollPane(this.tfList);
	tfpane.setBorder(BorderFactory.createTitledBorder("Found trials"));

	// chosen trials scroll pane
	this.tcList = new JList();
	JScrollPane tcpane = new JScrollPane(this.tcList);
	tcpane.setBorder(
		BorderFactory.createTitledBorder("Trials contained " +
						 "in the group"));

	// Close button
	JPanel cpanel = new JPanel();
	JButton closebtn = new JButton("Done");
	closebtn.setVerifyInputWhenFocusTarget(false);	
	closebtn.addActionListener(this);
	cpanel.add(closebtn);
	

	// Put it all together
	JPanel leftpane = new JPanel();
	leftpane.setLayout(new BoxLayout(leftpane, BoxLayout.Y_AXIS));
	leftpane.add(jp1);
	leftpane.add(tfpane);

	JPanel centerpane = new JPanel();
	centerpane.setLayout(new BoxLayout(centerpane, BoxLayout.X_AXIS));
	centerpane.add(leftpane);
	centerpane.add(tcpane);

	getContentPane().add(centerpane,BorderLayout.CENTER);
	getContentPane().add(cpanel,BorderLayout.SOUTH);

	pack();
    }

}
