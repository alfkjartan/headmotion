/** Unit test for AbsoluteScaledPower
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-04-08
 */

package kha.track;

import java.util.Hashtable;
import junit.framework.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;
import kha.math.KAlgebra;

public class AbsoluteScaledPowerTest extends TestCase {
    protected static DoubleFactory1D f1 = DoubleFactory1D.dense;
    protected static DoubleFactory2D f2 = DoubleFactory2D.dense;

    protected MotionData flexext;
    protected MotionData adabd;
    protected MotionData rotation;
    protected DoubleMatrix1D s1;
    protected DoubleMatrix1D s1min;
    protected DoubleMatrix1D c1;

    protected void setUp() {
	double samplingTime = 1;
	String[] units = {"mm","mm","mm","rad","rad","rad",
			  "mm/s","mm/s","mm/s","rad/s","rad/s","rad/s",
			  "mm/s2","mm/s2","mm/s2","rad/s2","rad/s2","rad/s2"};
	DoubleMatrix1D lp = KAlgebra.linspace(0,2*Math.PI,100);
	s1 = f1.make(100);
	s1min = f1.make(100);
	c1 = f1.make(100);
	for (int i=0; i<100; i++) {
	    s1.setQuick(i,Math.sin(lp.getQuick(i)));
	    s1min.setQuick(i,-Math.sin(lp.getQuick(i)));
	    c1.setQuick(i,Math.cos(lp.getQuick(i)));
	}

	DoubleMatrix2D fedata = f2.make(100,18);
	fedata.viewColumn(3).assign(s1);
	fedata.viewColumn(9).assign(c1);
	fedata.viewColumn(15).assign(s1min);

	flexext = new MotionData(fedata,new Hashtable(), 
				 samplingTime, units);

	DoubleMatrix2D abddata = f2.make(100,18);
	abddata.viewColumn(4).assign(s1);
	abddata.viewColumn(10).assign(c1);
	abddata.viewColumn(16).assign(s1min);

	adabd = new MotionData(abddata,new Hashtable(), 
				 samplingTime, units);
	
	DoubleMatrix2D rotdata = f2.make(100,18);
	rotdata.viewColumn(5).assign(s1);
	rotdata.viewColumn(11).assign(c1);
	rotdata.viewColumn(17).assign(s1min);

	rotation = new MotionData(rotdata,new Hashtable(), 
				 samplingTime, units);
    }
    
    public static Test suite() {
	return new TestSuite(AbsoluteScaledPowerTest.class);
    }
    
    public void testTransform() {
	DataTransform ast = new AbsoluteScaledPower();

	DoubleMatrix2D expected = f2.make(100,1);
	for (int i=0; i<100; i++) {
	    expected.setQuick(i,0,
			      Math.abs(c1.getQuick(i)*s1min.getQuick(i)));
	}

	try {
	    MotionData feresult = ast.transform(flexext);
	    MotionData abdresult = ast.transform(adabd);
	    MotionData rotresult = ast.transform(rotation);

	    assertEquals(feresult.getData(),expected);
	    assertEquals(abdresult.getData(),expected);
	    assertEquals(rotresult.getData(),expected);
	} catch (MalformedDataException ex){}
    }


    public static void main (String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}
