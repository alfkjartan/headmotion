/** 
 * Convenient linear algebra and matrix operations
 *
 * 2002-11-26
 * @author: Kjartan Halvorsen
 */

package kha.math;

import cern.colt.matrix.*;
import cern.colt.matrix.linalg.*;

public class KAlgebra { 

    private static DoubleFactory1D f1 = DoubleFactory1D.dense;
    private static DoubleFactory2D f2 = DoubleFactory2D.dense;
    private static DoubleFactory3D f3 = DoubleFactory3D.dense;
    private static Blas blas = SeqBlas.seqBlas;


    public static DoubleMatrix2D kron(DoubleMatrix2D A, DoubleMatrix2D B){
	int ma=A.rows();
	int na=A.columns();
	int mb=B.rows();
	int nb=B.columns();
	
	DoubleMatrix2D R = DoubleFactory2D.dense.make(ma*mb,na*nb);

	DoubleMatrix2D view;
	for (int i=0; i<ma; i++) {
	    for (int j=0; j<na; j++) {
		view=R.viewPart(i*mb,j*nb,mb,nb);
		view.assign(B);
		SeqBlas.seqBlas.dscal(A.getQuick(i,j),view);
	    }
	}
	return R;
    }

    public static DoubleMatrix1D linspace(double st, double en, int lngth) {
	if (lngth>0) {
	    DoubleMatrix1D tmp = DoubleFactory1D.dense.make(lngth);
	    double incr;
	    if (lngth==1) {
		incr = 0;
	    } else {
		incr = (en-st)/((double) (lngth-1));
	    }
	    for (int i=0; i<lngth; i++) {
		tmp.setQuick(i,st+incr*i);
	    }
	    return tmp;
	} else {
	    return null;
	}
    }
	

    // TODO: check size of arguments
    public static void vec(DoubleMatrix2D M, DoubleMatrix1D v) {
	int rows = M.rows();
	for (int i=0; i<M.columns(); i++) {
	    v.viewPart(i*rows,rows).assign(M.viewColumn(i));
	}
    }


    public static DoubleMatrix1D  cross (DoubleMatrix1D a, 
					 DoubleMatrix1D b) 
	throws IllegalArgumentException {
	if (a.size()!=3 | b.size()!=3)
	    throw new 
		IllegalArgumentException("Arguments must have length 3.");
	
	DoubleMatrix2D ahat = JointModel.hat(a);
	return Algebra.DEFAULT.mult(ahat,b);
    }

    
    public static DoubleMatrix1D rowSum (DoubleMatrix2D M) {
	DoubleMatrix1D tmp = f1.make(M.rows());
	for (int i=0; i<M.columns(); i++) {
	    blas.daxpy(1,M.viewColumn(i),tmp);
	}
	return tmp;
    }



    public static void main (String[] args) {
	DoubleFactory2D f2 = DoubleFactory2D.dense;
	DoubleFactory1D f1 = DoubleFactory1D.dense;
	DoubleMatrix1D ra = f1.make(new double[]{1,2,3});
	DoubleMatrix2D A = f2.diagonal(ra);

	DoubleMatrix2D B = f2.make(2,2,2.0);
	
	DoubleMatrix2D C = KAlgebra.kron(A,B);

	System.out.println("Matrix A");
	System.out.println(A);
	System.out.println("Matrix B");
	System.out.println(B);
	System.out.println("Matrix A kron B");
	System.out.println(C);

	DoubleMatrix1D lsp = KAlgebra.linspace(1,20,20);
	System.out.println("Linspace 1 to 20");
	System.out.println(lsp);

	A = f2.random(4,3);
	DoubleMatrix1D y = f1.make(12);

	vec(A,y);
	System.out.println("Matrix A");
	System.out.println(A);
	System.out.println("vec A");
	System.out.println(y);

       
    }
}
    
