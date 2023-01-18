/** KAlgebraTest.java 
 * @date: 2003-03-28
 * @author: Kjartan Halvorsen
 * Unit tests for KAlgebra.java
 */

package kha.math;

import junit.framework.*;
import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;

/**
 * Class KAlgebraTest
 *
 */
public class KAlgebraTest extends TestCase {
    protected static DoubleFactory1D f1 = DoubleFactory1D.dense;
    protected static DoubleFactory2D f2 = DoubleFactory2D.dense;
    protected static DoubleFactory3D f3 = DoubleFactory3D.dense;

    protected DoubleMatrix2D I33;
    protected DoubleMatrix2D I44;
    protected DoubleMatrix2D A33;
    protected DoubleMatrix2D B33;
    protected DoubleMatrix2D C44;
    protected DoubleMatrix2D D44;

    protected DoubleMatrix1D v3;
    protected DoubleMatrix1D u3;
    protected DoubleMatrix1D v4;
    protected DoubleMatrix1D u4;


    protected void setUp() {
	I33 = f2.identity(3);
	I44 = f2.identity(3);
	A33 = f2.random(3,3);
	B33 = f2.random(3,3);
	C44 = f2.random(4,4);
	D44 = f2.random(4,4);

	u3 = f1.random(3);
	v3 = f1.random(3);
	u4 = f1.random(4);
	v4 = f1.random(4);
    }
    
    public static Test suite() {
	return new TestSuite(KAlgebraTest.class);
    }
    
    public void testKron() {
	DoubleMatrix2D result = KAlgebra.kron(A33,C44);
	assertTrue(result.columns() == 3*4);
	assertTrue(result.rows() == 3*4);
	DoubleMatrix2D expected = C44.copy();
	SeqBlas.seqBlas.dscal(A33.get(0,0),expected);
	assertEquals(result.viewPart(0,0,4,4),expected);
	
    }

    public void testVec() {
	DoubleMatrix1D vv = KAlgebra.linspace(0.0,6.0,7);
	assertTrue(vv.size() == 7);
	assertTrue(vv.get(6) == 6.0);
	
	DoubleMatrix2D M = f2.make(7,1);
	M.viewColumn(0).assign(vv);

	DoubleMatrix1D result = f1.make(7);
	KAlgebra.vec(M,result);
	assertEquals(result,vv);

    }


    public void testCross() {
	try {
	    KAlgebra.cross(u3,v4);
	    fail("Expected IllegalArgumentException");
	} catch (Exception e) {
	    assertTrue(e instanceof IllegalArgumentException);
	}
	
	DoubleMatrix1D result = KAlgebra.cross(u3,v3);
	
	double delta = Property.DEFAULT.tolerance();
	assertEquals(result.get(0),
		     u3.get(1)*v3.get(2) - u3.get(2)*v3.get(1),
		     delta);
	assertEquals(result.get(1),
		     u3.get(2)*v3.get(0) - u3.get(0)*v3.get(2),
		     delta);
	assertEquals(result.get(2),
		     u3.get(0)*v3.get(1) - u3.get(1)*v3.get(0),
		     delta);
    }

    public static void main (String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}
