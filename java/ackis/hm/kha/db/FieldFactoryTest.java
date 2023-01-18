/**
 * Unit tests for the FieldFactory class and its inner classes.
 *
 * @author  Kjartan Halvorsen
 * @version 1.0  2003-04-10
 */

package kha.db;

import junit.framework.*;

import javax.swing.*;
import java.awt.*;

public class FieldFactoryTest extends TestCase {
    protected static FieldFactory ff = FieldFactory.FF;

    protected Field aStringField;
    protected Field anIntegerField;
    protected Field aFloatField;
    protected Field aDateField;
    protected Field aYesNoField;

    protected JFrame testGUI;

    protected void setUp() {
	aStringField = ff.createField(FieldFactory.STRING,
				      "string_field",
				      "String Field",
				      true,
				      true);

	anIntegerField = ff.createField(FieldFactory.INT,
				      "int_field",
				      "Integer Field",
				      true,
				      true);

	aFloatField = ff.createField(FieldFactory.FLOAT,
				      "float_field",
				      "Float Field",
				      true,
				      false);

	aDateField = ff.createField(FieldFactory.DATE,
				      "date_field",
				      "Date Field",
				      true,
				      true);

	aYesNoField = ff.createYesNoField("yesno",
					  "Yes or No",
					  true);

	// Add the gui for each database field
	JPanel afieldp = new JPanel();
	JPanel sfieldp = new JPanel();
	GridBagLayout agridb = new GridBagLayout();
	GridBagLayout sgridb = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();
	afieldp.setLayout(agridb);
	sfieldp.setLayout(sgridb);
	
	Field[] fields = {aStringField, anIntegerField, 
			   aFloatField, aDateField};

	for (int row = 0; row<4; row++) {
	    gbc.gridy = row;
	    gbc.weightx = 0.5;
	    gbc.fill = GridBagConstraints.HORIZONTAL; 

	    gbc.gridx = 0;
	    JLabel descr = new JLabel(fields[row].getName(),JLabel.TRAILING);
	    sgridb.setConstraints(descr,gbc);
	    agridb.setConstraints(descr,gbc);
	    afieldp.add(descr);
	    sfieldp.add(descr);

	    gbc.gridx = 1;
	    Component fgui = fields[row].getAddGUI();
	    Component sgui = fields[row].getSearchGUI();
	    agridb.setConstraints(fgui,gbc);
	    afieldp.add(fgui);

	    if (sgui != null) {
		sgridb.setConstraints(sgui,gbc);
		sfieldp.add(sgui);
	    }

	}

	testGUI = new JFrame("FieldFactoryTest");

	testGUI.getContentPane().add(afieldp);
	testGUI.getContentPane().add(sfieldp);
	testGUI.pack();
        testGUI.setVisible(true);
    }


    public static Test suite() {
	return new TestSuite(FieldFactoryTest.class);
    }

    public void testSearchString() {
	String tstr = "tjena";
	JComboBox sb = (JComboBox) aStringField.getSearchGUI();
	sb.insertItemAt(tstr,0);
	sb.setSelectedItem(tstr);

	String expected = "string_field = 'tjena'";
	String result = aStringField.getSQLSearchString();

	System.err.println(expected);
	System.err.println(result);
	
	assertEquals(expected, result);

	tstr = ">4";
	JTextField tf = (JTextField) anIntegerField.getSearchGUI();
	tf.setText(tstr);
	tf.postActionEvent();

	expected = "int_field > 4";
	result = anIntegerField.getSQLSearchString();

	System.err.println(expected);
	System.err.println(result);
	
	assertEquals(expected, result);

    }

    public void testYesNo() {
	Boolean yes = new Boolean(true);
	Boolean no = new Boolean(false);

	String val = (String) aYesNoField.getValue();
	System.err.println(val);
	assertEquals("No",val);

	aYesNoField.setValue("yes");
	val = (String) aYesNoField.getValue();
	System.err.println(val);
	assertEquals("Yes",val);

	aYesNoField.setValue(no);
	System.err.println(val);
	val = (String) aYesNoField.getValue();
	assertEquals("No",val);
    }
	
    
    public void testNumberField() {
	Integer intval = new Integer(12);
	Object ointval = new Integer(12);
	assertEquals(intval,ointval);
	System.err.println("Setting value of integer field");
	anIntegerField.setValue(intval);
	assertEquals(intval,anIntegerField.getValue());

	System.err.println(intval);

	anIntegerField.setValue(intval.toString());
	assertEquals(intval,anIntegerField.getValue());

	Double dval = new Double(12.0);
	aFloatField.setValue(dval);
	assertEquals(dval,aFloatField.getValue());

	System.err.println(aFloatField.getValue());

	JTextField inttf = (JTextField) anIntegerField.getAddGUI();
	JTextField ftf = (JTextField) aFloatField.getAddGUI();

	inttf.setText("42");
	ftf.setText("42.0");
	
	System.err.println(inttf.getText());

	assertEquals(new Double(42.0),aFloatField.getValue());
	assertEquals(new Integer(42),anIntegerField.getValue());

	aFloatField.setValue("hej");
	anIntegerField.setValue("tjena");
	assertEquals(new Double(42.0),aFloatField.getValue());
	assertEquals(new Integer(42),anIntegerField.getValue());

	ftf.setText("hej");
	inttf.setText("tjena");
	assertEquals(new Double(1.0),aFloatField.getValue());
	assertEquals(new Integer(-1),anIntegerField.getValue());



    }
	
	
    public static void main (String[] args) {
	    junit.textui.TestRunner.run(suite());
	    //FieldFactoryTest fft = new FieldFactoryTest();
	    //fft.setUp();
    }

}

	
