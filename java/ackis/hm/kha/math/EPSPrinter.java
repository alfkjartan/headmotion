/**
 * Class which handles eps printing of a Plotter object.
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-04-15
 */

package kha.math;

import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import ptolemy.plot.EPSGraphics;
import kha.hm.*;

public class EPSPrinter implements ActionListener {

    private Plotter plot; 

    public EPSPrinter(Plotter pl) {
	this.plot = pl;
    }

    public void actionPerformed(ActionEvent ev) {
	//Create a file chooser
	JFileChooser fc = new JFileChooser();
	
	ExtensionFileFilter mfilter = 
	    new ExtensionFileFilter("eps",
				    "encapsulated postscript");
	fc.setFileFilter(mfilter);
	int returnVal = fc.showSaveDialog(this.plot);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = new File("Unnamed");
	    try {
		file = fc.getSelectedFile();
		OutputStream out = new FileOutputStream(file);
		int width_inchs = 6;
		int height_inchs = 10;
		this.plot.export(out);
	//this.plot.paintComponents(new EPSGraphics(out, width_inchs*72,
	//						height_inchs*72));
		out.flush();
		out.close();
	    } catch (IOException ioex) {
		System.err.println("Unable to write to file:");
		System.err.println(file.toString());
	    }
	}
    }
}
