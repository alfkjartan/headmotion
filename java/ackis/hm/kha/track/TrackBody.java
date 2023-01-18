/** 
 * Class for 6 DOF model used in tracking
 *
 * 2003-02-12
 * @author: Kjartan Halvorsen
 */

package kha.track;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import kha.math.*;
import kha.tsv.*;

public class TrackBody {
    private final double bandwidthscale = 100000;

    private TSVReader tsvfile = null;
    private String filename;
    private String[] markerset;
    private double bandwidth=1;
    private DoubleMatrix2D states;
    private DoubleMatrix3D estimcov;

    public static final int MARKER_NOT_FOUND = 32101;
    public static final int KALMAN_FILTER_FAILED = 32102;

    private static DoubleFactory1D f1 = DoubleFactory1D.dense;
    private static DoubleFactory2D f2 = DoubleFactory2D.dense;
    private static DoubleFactory3D f3 = DoubleFactory3D.dense;
    private static Algebra alg = Algebra.DEFAULT;

    public TrackBody(String file, double bandwidth, String[] mset) {
	this.filename=file;
	this.bandwidth=bandwidth;
	this.markerset=mset;
	try {
	    BufferedReader filereader = 
		new BufferedReader( new  FileReader(file));
	    this.tsvfile = new TSVReader(filereader);
	} catch (IOException e) {
	    JOptionPane.showMessageDialog(null,"Unable to open file: " + file);
	}
    }

    public TrackBody(DoubleMatrix2D data, Hashtable attr, 
		     double bandwidth, String[] mset) {
	this.filename="";
	this.bandwidth=bandwidth;
	this.markerset=mset;
	this.tsvfile = new TSVReader(data,attr);
    }


    public TrackBody(String f,DoubleMatrix2D data, Hashtable attr, 
		     double bandwidth, String[] mset) {
	this.filename=f;
	this.bandwidth=bandwidth;
	this.markerset=mset;
	this.tsvfile = new TSVReader(data,attr);
    }

    /**
     * Returns true if file was read, false if not.
     */
    public boolean isOK() {
	return (this.tsvfile != null);
    }

    public DoubleMatrix2D stateEstimates() {
	return states;
    }

    public DoubleMatrix3D covEstimates() {
	return estimcov;
    }


    public DoubleMatrix2D getMarkerData() {
	return tsvfile.getAllData();
    }

    /** 
     *Returns the attributes hash
     */
    public Hashtable getAttributes() {
	return tsvfile.getAttributes();
    }


    public void track(double b) throws TrackException {
	this.bandwidth = b;
	track();
    }
    
    public int track() throws TrackException {
	DoubleMatrix2D mdata = tsvfile.getMarkerSetData(markerset);

	if (mdata.columns() != (markerset.length*3))   
	    throw new TrackException("Marker not found.",
				     tsvfile.getMarkerNames());


	int cols=mdata.columns();
	int nmrks=cols/3;
	int nfrs=mdata.rows();

	states=f2.make(18,nfrs);
	estimcov=f3.make(18,18,nfrs);

	double samplefreq;
	try {
	    samplefreq = 
		Double.parseDouble(tsvfile.getValue("FREQUENCY"));
	} catch (NumberFormatException e) {
	    System.err.println("Could not find or parse FREQUENCY" +
			       " attribute of tsv file.");
	    System.err.println(e.getMessage());
	    samplefreq=240;
	}
	double dt = 1.0/samplefreq;

	// Construct a noise intensity matrix
	DoubleMatrix1D ra = f1.make( new double[] 
	    {1,1,1,Math.pow(Math.PI/12,2),Math.pow(Math.PI/12,2),
	     Math.pow(Math.PI/12,2)});
	SeqBlas.seqBlas.dscal(bandwidth*bandwidthscale,ra);
	DoubleMatrix2D Ra = f2.diagonal(ra);
	
	// Construct a measurement noise matrix
	DoubleMatrix1D r2 = f1.make(nmrks*3,1.0);
	DoubleMatrix2D R2 = f2.diagonal(r2);

	// Read the first row of data.
	DoubleMatrix1D row1=mdata.viewRow(0);
	DoubleMatrix2D p0 = f2.make(row1.toArray(),3);
	System.err.println(row1);
	System.err.println(p0);

	
	// The centroid of the markers
	DoubleMatrix1D c = KAlgebra.rowSum(p0);

	// Construct the twists
	DoubleMatrix1D e_x = f1.make(new double[] {1,0,0});
	DoubleMatrix1D e_y = f1.make(new double[] {0,1,0});
	DoubleMatrix1D e_z = f1.make(new double[] {0,0,1});

	DoubleMatrix2D tws = f2.identity(6);
	tws.viewColumn(3).viewPart(0,3).assign(KAlgebra.cross(c,e_y));
	tws.viewColumn(3).viewPart(3,3).assign(e_y);
	tws.viewColumn(4).viewPart(0,3).assign(KAlgebra.cross(c,e_x));
	tws.viewColumn(4).viewPart(3,3).assign(e_x);
	tws.viewColumn(5).viewPart(0,3).assign(KAlgebra.cross(c,e_z));
	tws.viewColumn(5).viewPart(3,3).assign(e_z);

	p0 = f2.appendRows(p0,f2.make(1,nmrks,1));

	// Construct a JointModel
	JointModel jm = new JointModel(tws);

	// .. and a state space model
	StateSpaceModel mod = new RigidBodyTracker(Ra,R2,dt,jm,p0);

	// Initial estimate and cov matrix
	DoubleMatrix1D x0 = f1.make(18);
	DoubleMatrix2D P0 = f2.make(18,18,20);


	// Run the fixed-interval EKF
	int kf_res = Kalman.fix_interval(mdata.viewDice(),
					x0,P0,mod,states,estimcov);
	if (kf_res != 0) throw new TrackException("Kalman filter failed",
						  true);
	else return kf_res;
    }

    public void plot() {
	this.plot(null);
    }

    public void plot(String[] headers) {
	DoubleMatrix2D plotdata = states.copy();
	SeqBlas.seqBlas.dscal(180.0/Math.PI,plotdata);
	
	Plotter aplot = new Plotter(filename,3);
	aplot.setHeaders(headers);

	aplot.addData(plotdata.viewRow(4));
	aplot.addData(plotdata.viewRow(3));
	aplot.addData(plotdata.viewRow(5));
	
	aplot.getCurrentPlot().setTitle("Rotations");
	aplot.getCurrentPlot().setYLabel("degrees");
	aplot.getCurrentPlot().addLegend(0,"About the x-axis");
	aplot.getCurrentPlot().addLegend(1,"About the y-axis");
	aplot.getCurrentPlot().addLegend(2,"About the z-axis");

	aplot.setCurrentPlot(1);
	aplot.addData(plotdata.viewRow(10));
	aplot.addData(plotdata.viewRow(9));
	aplot.addData(plotdata.viewRow(11));
	aplot.getCurrentPlot().setTitle("Rotation velocities");
	aplot.getCurrentPlot().setYLabel("degrees/s");
	aplot.getCurrentPlot().addLegend(0,"About the x-axis");
	aplot.getCurrentPlot().addLegend(1,"About the y-axis");
	aplot.getCurrentPlot().addLegend(2,"About the z-axis");


	aplot.setCurrentPlot(2);
	aplot.addData(plotdata.viewRow(16));
	aplot.addData(plotdata.viewRow(15));
	aplot.addData(plotdata.viewRow(17));
	aplot.getCurrentPlot().setTitle("Rotation accelerations");
	aplot.getCurrentPlot().setYLabel("degrees/s^2");
	aplot.getCurrentPlot().addLegend(0,"About the x-axis");
	aplot.getCurrentPlot().addLegend(1,"About the y-axis");
	aplot.getCurrentPlot().addLegend(2,"About the z-axis");

    }

    public static void main (String[] args) {
	// Check the number of arguments
	if (args.length<2) {
	System.err.println("Wrong number of arguments");
	System.err.println("Usage:");
	System.err.println(">java kha.track.TrackBody tsvfile bandwidth");
	System.exit(1);
	}

	// Parse the bandwidth parameter
	double bw=Double.parseDouble(args[1]);

	// harcoded markerset
	String[] markerset = new String[]{"huvud1","huvud2","huvud3","huvud4"};
	TrackBody tb = new TrackBody(args[0],bw,markerset);

	try {
	    tb.track();
	} catch (TrackException e) {}
	tb.plot();
    }
}



	
	
	
	

	
		     
