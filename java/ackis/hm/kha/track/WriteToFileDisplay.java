/**
 * Writes the data to a file
 *
 * @author   Kjartan Halvorsen
 * @version  1.0   2003-04-28
 */

package kha.track;

import java.io.*;
import javax.swing.filechooser.*;
import javax.swing.JFileChooser;
import java.util.Vector;
import cern.colt.matrix.*;
import kha.math.*;
import kha.hm.*;

public class WriteToFileDisplay extends Display {
    
    /**
     * Asks the user for a text file to write the results to.
     * Assumes that each MotionData object in the set is a vector.
     * @param  vec  the set of MotionData objects
     */
    public void displaySet(Vector vec) {
	MotionData md = (MotionData) vec.elementAt(0);
	DoubleMatrix2D data = 
	    DoubleFactory2D.dense.make(md.frames(),vec.size()); 
	String[] headings = new String[vec.size()];
	for (int i=0; i<vec.size(); i++) {
	    md = (MotionData) vec.elementAt(i);
	    data.viewColumn(i).assign(md.getData().viewColumn(0));
	    headings[i] = md.toString();
	}
	
	dumpToFile(data,headings);
	
    }


    private void dumpToFile(DoubleMatrix2D data,
			    String[] headings) {
	//Create a file chooser
	JFileChooser fc = new JFileChooser();
	
	ExtensionFileFilter mfilter = 
	    new ExtensionFileFilter("txt",
				    "text files");
	fc.setFileFilter(mfilter);
	int returnVal = fc.showSaveDialog(null);
	if (returnVal == JFileChooser.APPROVE_OPTION) {
	    File file = new File("Unnamed");
	    try {
		file = fc.getSelectedFile();
		PrintWriter out = new PrintWriter(
			  new BufferedWriter(new FileWriter(file)));
		for (int i=0;i<headings.length-1;i++) {
		    out.print(headings[i]);
		    out.print("\t");
		}
		out.print(headings[headings.length-1]);
		out.println();
		
		for (int i=0; i<data.rows(); i++) {
		    for (int j=0; j<data.columns()-1; j++) {
			out.print(data.getQuick(i,j));
			out.print("\t");
		    }
		    out.print(data.getQuick(i,data.columns()-1));
		    out.println();
		}
		out.flush();
		out.close();
	    } catch (IOException ioex) {
		System.err.println("Unable to write to file:");
		System.err.println(file.toString());
	    }
	}
    }

}

