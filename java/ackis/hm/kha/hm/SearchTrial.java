/**
 *  GUI for searching among the trials in the database
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-04-28
 */

package kha.hm;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import cern.colt.matrix.*;

import kha.db.*;
import kha.track.*;

/**
 * Represents the "Search" tab in the gui. 
 *
 * Revisions
 * 2006-09-28  Added a button "Search subject", which will open up the subject                dialog. Upon closing the dialog, the chosen subject will be 
 *             indicated in the subject field of the search tab
 */
public class SearchTrial extends JPanel implements ActionListener {

    private static SearchTrial theGUI = null;

    private HeadMotionDB database;

    private JTabbedPane tabpane;
    private TrialGUI trialgui;
    private Vector trialfields;

    private Vector trialsFound;
    private JList tfList;
    
    private SearchTrial(HeadMotionDB database, TrialGUI tg, JTabbedPane tp) {
	super();
	
	this.database = database;
	this.trialgui = tg;
	this.tabpane = tp;
	this.trialfields = tg.getFields();
	this.trialsFound = new Vector();

	createGUI();
    }

    public static SearchTrial getInstance(HeadMotionDB db, TrialGUI tg,
					  JTabbedPane tp) {
	if (theGUI == null) {
	    theGUI = new SearchTrial(db, tg, tp);
	}
	return theGUI;
    }

    /**
     * Returns a reference to the Singleton. Be careful not to call
     * this before it is certain that the instance has been created.
     * @return  a reference to the Singleton, or null if not yet
     *          created.
     */
    public static SearchTrial getInstance() {
	return theGUI;
    }

    public static ActionListener getActionListener() {
	return theGUI;
    }

    /**
     * Set the list of trials
     */
    void setTrialsFound(Vector vec) {
	this.trialsFound.clear();
	for (Iterator it = vec.iterator(); it.hasNext();) {
	    Object obj = it.next();
	    if (obj instanceof Trial) 
		this.trialsFound.add(obj);
	}
	this.tfList.setListData(this.trialsFound);
    }
		
    /**
     * Shows the process tab, which is assumed to be the first in the
     * tabbed pane.
     */
    void showProcessTab() {
	this.tabpane.setSelectedIndex(0);
    }

    /**
     * Shows this tab. Assumed to be the second.
     */
    void showMyTab() {
	this.tabpane.setSelectedIndex(1);
    }

    /** 
     * The ActionEvent either indicates that the chosen Trial should be 
     * edited in the "Process" tab, or clears the list of trials
     */
    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("Edit trial")) {
	    Object[] selected = this.tfList.getSelectedValues();
	    if (selected.length>0) {
		this.trialgui.setFields((Trial) selected[0]);
		this.showProcessTab();
	    }
	} else if (e.getActionCommand().equals("Clear list")) {
	    this.clearList();
	} else if (e.getActionCommand().equals("Delete trials")) {
	    this.deleteTrial();
	}
    }
    
    private void deleteTrial() {
	// Warn user that all chosen trials will be removed from the database
	if (HeadMotion.hm.askUserYesNo("Delete all selected trials " +
				       "from the database?")) {
	    int numDeleted=0;
	    Object[] selected = this.tfList.getSelectedValues();
	    if (selected.length>0) {
		for (int i=0; i< selected.length; i++) {
		    int kk = 
			((Trial) selected[i]).removeFromDatabase(this.database);
		    if (kk==1) {
			numDeleted++;
			this.trialsFound.remove(selected[i]);
		    }
		}
	    }
	
	    // Remove the selected rows
	    HeadMotion.hm.tellUser("Total of " + numDeleted +
				   " trials removed from the database.");
	    this.tfList.setListData(this.trialsFound);
	
	    AnalyzeGUI.getGroupChooser().reloadGroupsFromDatabase();
	}
    }

	    
    private void clearList() {
	this.trialsFound.clear();
	this.tfList.setListData(this.trialsFound);
    }

    /**
     * The actionPerformed when pressing the "Search" button ends
     * up here.
     */
    private void searchTrials() {
	this.clearList();
	StringBuffer sqlstr = new StringBuffer("");
	for (Enumeration en = trialfields.elements(); 
	     en.hasMoreElements();) {
	    Field f = (Field) en.nextElement();
	    String sstr = f.getSQLSearchString();
	    if (sstr.length()>2) {
		sqlstr.append(sstr);
		sqlstr.append(" AND ");
	    }
	}
	
	if (sqlstr.length()>4)
	    sqlstr.delete(sqlstr.length()-5,sqlstr.length());
	
	Trial[] foundtrials = 
	    this.database.searchTrials(sqlstr.toString());

	System.err.println("found trials: " + foundtrials.length);
	
	// add the results to the list of trials found
	for (int i=0; i<foundtrials.length; i++) {
	    addIfUnique(this.trialsFound,foundtrials[i]);
	}
	this.tfList.setListData(this.trialsFound);
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

	// Make the gui for setting search criteria
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


	// Make button
	JPanel btnp = new JPanel();
	JButton srchbtn = new JButton("Search for trials");
	srchbtn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    searchTrials();
		}
	    });
	btnp.add(srchbtn);

	JPanel jp1 = new JPanel();
	jp1.setLayout(new BorderLayout());
	jp1.add(fieldp,BorderLayout.CENTER);
	jp1.add(btnp,BorderLayout.SOUTH);

	
	// Found trials scroll pane
	this.tfList = new JList();
	JScrollPane tfpane = new JScrollPane(this.tfList);
	tfpane.setBorder(BorderFactory.createTitledBorder("Found trials"));

	// Put it all together
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	add(jp1);
	add(tfpane);
    }

}
