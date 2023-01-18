/** 
 * GUI for choosing tsv files
 *
 * @author  Kjartan Halvorsen
 * @version 1.0   2003-04-09
 */

package kha.hm;

import javax.swing.filechooser.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import kha.db.*;

public class FileChooser extends AbstractField {

    private JPanel jp;
    private ActionTextField fileArea;


    /**
     * Constructs FileChooser field with given heading and name, the
     * ActionListener will listen to changes in the text field
     */
    public FileChooser(String heading, String name, 
		       ActionListener listener) {
	this(heading,name);
	fileArea.addActionListener(listener);
	fileArea.setActionCommand("New trial");
    }

    public FileChooser(String heading, String name) {
	super(true,false,heading,name);
	setVerifier(VerifierFactory.isFile());

	this.jp = new JPanel();
	jp.setLayout(new BoxLayout(jp,BoxLayout.X_AXIS));
	//JLabel label = new JLabel("TSV file:");
	fileArea = new ActionTextField(20);
	//label.setLabelFor(fileArea);
	//jp.add(label);
	jp.add(fileArea);

	//Create a file chooser
	final JFileChooser fc = new JFileChooser();
	final ExtensionFileFilter mfilter = 
	    new ExtensionFileFilter(new String[] {"tsv","TSV"},
				    "tsv data filer");


	JButton btn = new JButton ("Browse");
	btn.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    fc.setFileFilter(mfilter);
		    int returnVal = fc.showOpenDialog(jp);

		    if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileArea.setText(fc.getSelectedFile().toString());
			fileArea.fireActionPerformed();
		    }
		}
	    });

	jp.add(btn);

	jp.setBorder(BorderFactory.createTitledBorder("Measurement file"));

    }

    public Component getAddGUI() {
	return this.jp;
    }

    public Object getValue() {
	return this.getFile();
    }

    public void setValue(Object o) {
	this.fileArea.setText(o.toString());
    }

    public void writeValue(PreparedStatement st, int pos)
	throws SQLException {
	st.setString(pos,getFile());
    }

    public String getFile() {
	return fileArea.getText();
    }
}


/**
 * Extends the JTextField class, only to be able to fire actionPerformed
 * events.
 */
class ActionTextField extends JTextField {

    public ActionTextField (int size) {
	super(size);
    }

    public void fireActionPerformed() {
	super.fireActionPerformed();
    }
}

     
