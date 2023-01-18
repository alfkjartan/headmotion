/**
 * Unit tests for the Wilcoxon signed rank test comparison class
 *
 * @author  Kjartan Halvorsen
 * @version 1.0  2003-08-22
 */

package kha.track;

import java.io.*;
import java.util.Hashtable;
import junit.framework.*;
import cern.colt.list.*;


public class WilcoxonTestUnitTest extends TestCase {

    protected WilcoxonSRTestComparison srcomp = new WilcoxonSRTestComparison();
    protected WilcoxonTestComparison comp = new WilcoxonTestComparison();

    protected void setUp() {}

    public void tearDown() {}

    public static Test suite() {
	return new TestSuite(WilcoxonTestUnitTest.class);
    }

    

    public void testRealData() {

	DoubleArrayList controls = new DoubleArrayList( 30 );
	DoubleArrayList patients = new DoubleArrayList( 30 );
	
	BufferedReader in_pc;
    
	try {
	    in_pc = 
		new BufferedReader( new FileReader("/home/kjartan/ackis/" +
						   "figures/" +
						   "controls_patients_030918.txt")
				    );

	    // Known that the data has 30 rows
	    String datastr = in_pc.readLine(); // The header
	    for (int i=0; i<30; i++) {
		String[] dataline = in_pc.readLine().split("\\s"); 
		controls.add(Double.parseDouble(dataline[1]));
		patients.add(Double.parseDouble(dataline[2]));
	    }
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}

	comp.wilcoxon2GroupCompare(controls, patients);
	Hashtable wResult = comp.getResults(); 

	Double p = (Double) wResult.get("p");
	Double Wsmallest = (Double) wResult.get("Wsmallest");



	DoubleArrayList controlsd1 = new DoubleArrayList( 25 );
	DoubleArrayList controlsd2 = new DoubleArrayList( 25 );
	
    
	try {
	    in_pc = 
		new BufferedReader( new FileReader("/home/kjartan/ackis/" +
						   "figures/" +
						   "test_retest_controls.txt")
				    );

	    // Known that the data has 25 rows
	    String datastr = in_pc.readLine(); // The header
	    for (int i=0; i<25; i++) {
		String[] dataline = in_pc.readLine().split("\\s"); 
		controlsd1.add(Double.parseDouble(dataline[1]));
		controlsd2.add(Double.parseDouble(dataline[2]));
	    }
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	}

	srcomp.wilcoxonCompare(controlsd1, controlsd2);
	wResult = srcomp.getResults(); 

	p = (Double) wResult.get("p");
	Double Wpluss = (Double) wResult.get("W+");
	Double Wmin = (Double) wResult.get("W-");

    }

						  
    public void testSRComparison() {
	DoubleArrayList data1 = new DoubleArrayList( new double[] {595,
								   523,
								   560,
								   548,
								   589,
								   576,
								   545,
								   501,
								   582,
								   544,
								   561,
								   579,
								   592,
								   573,
								   517,
								   540,
								   593,
								   591,
								   541,
								   589,
								   505,
								   535,
								   581,
								   500,
								   513});


	DoubleArrayList data2 = new DoubleArrayList( new double[] {520,
								   519,
								   560,
								   527,
								   519,
								   501,
								   574,
								   544,
								   593,
								   546,
								   541,
								   584,
								   552,
								   520,
								   567,
								   583,
								   501,
								   568,
								   537,
								   583,
								   550,
								   570,
								   542,
								   530,
								   518});
								   

	srcomp.wilcoxonCompare(data1, data2);
	Hashtable wResult = srcomp.getResults(); 

	double expected_p = 0.449;
	double expectedWpluss = 177;
	double expectedWmin = 123;

	Double p = (Double) wResult.get("p");
	Double Wpluss = (Double) wResult.get("W+");
	Double Wmin = (Double) wResult.get("W-");

	
	assertTrue( Math.abs( expected_p - p.doubleValue()) < 0.001);
	assertTrue( expectedWpluss == Wpluss.doubleValue());
	assertTrue( expectedWmin == Wmin.doubleValue());
    }
	
    public void testComparison() {
	DoubleArrayList data1 = new DoubleArrayList( new double[] {579,
								   509,	
								   527,
								   516,
								   592,
								   503,
								   511,
								   517,
								   538,
								   549,
								   557,
								   568,
								   582,
								   553,
								   424});


	DoubleArrayList data2 = new DoubleArrayList( new double[] {594,
								   513,
								   566,
								   588,
								   584,
								   510,
								   535,
								   514,
								   582,
								   567,
								   593,
								   574,
								   597,
								   558,
								   461});

	comp.wilcoxon2GroupCompare(data1, data2);
	Hashtable wResult = comp.getResults(); 

	double expected_p = 0.1198;
	double expectedW = 194.5;

	Double p = (Double) wResult.get("p");
	Double W = (Double) wResult.get("Wsmallest");
	
	assertTrue( Math.abs( expected_p - p.doubleValue()) < 0.001);
	assertTrue( expectedW == W.doubleValue());
    }
	
	
    public static void main (String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}

	
