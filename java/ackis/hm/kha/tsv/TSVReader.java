/** 
  Reads tsv data from an InputStream
  */

// Original code:
// Kjartan Halvorsen
// 2000-04-13
// 
// Rewritten to use the cern.colt package
// 2002-11-25

package kha.tsv;

import java.util.*;
import java.net.*;
import java.io.*;
import cern.colt.matrix.*;
import kha.math.doubleStream;
//import set.*;

public class TSVReader
{
  private DoubleMatrix2D data;
  private Hashtable attributes;
  private Hashtable mdata;

  public TSVReader(InputStream is) {
      this(new BufferedReader(new InputStreamReader(is)));
  }

  public TSVReader(InputStreamReader isr) {
      this(new BufferedReader(isr));
  }
  
  public TSVReader(BufferedReader bufr) {
    try {
	parseTSV(bufr);
    } catch (Exception e) {
	System.err.println("Not able to parse .tsv file");
	System.err.println(e.getMessage());
    }
    close(bufr);
    splitMarkerData();
  }

    public TSVReader(DoubleMatrix2D data, Hashtable attr) {
	this.data = data;
	this.attributes = attr;
	splitMarkerData();
    }

  private void close(BufferedReader bufr) {
      try {
	  if (bufr != null) bufr.close();
      } catch (Exception e){}
  }

   
  private void parseTSV(BufferedReader bufr) throws IOException{
      attributes = new Hashtable();
      DoubleMatrix2D row;
      boolean isDataline = false;
      boolean gotFirstRow = false;
      DoubleFactory2D factory = DoubleFactory2D.dense;

      String line = bufr.readLine();
    
      while (line != null) {
	  
	  StringTokenizer st = new StringTokenizer(line);
	  row=factory.make(1,st.countTokens());
	  int j=0;
	  while (st.hasMoreTokens()) {
	      String theToken = st.nextToken();
	      try {
		  row.set(0,j,Double.parseDouble(theToken));
		  j++;
		  // System.err.println("Parsed token: " + theToken);
		  isDataline=true;
	      } catch (NumberFormatException e) {

		  // If the file is not corrupt, then the exception 
		  // occured for the header lines. Add attribute to 
		  // hashtable
		  StringBuffer value = new StringBuffer("");
		  while (st.hasMoreTokens()) {
		      //  System.err.println("Adding attribute " + theToken);
		      value.append("\t");
		      value.append(st.nextToken());
		  }
		  attributes.put(theToken,value.toString());
	      } catch (Exception ee) {
		  System.err.println(ee.getMessage());
	      }
	  }
	  if (isDataline) {
	      if (gotFirstRow) {
		  data = factory.appendRows(data,row);
		  //System.err.println("Appended row");
	      } else {
		  data=row;
		  gotFirstRow=true;
		  //System.err.println("Read first data row");
	      }
	  }
	  line = bufr.readLine();
      }
  }

  // Divide the data, and associate each trajectory with the 
  // name
  private void splitMarkerData() {
      mdata = new Hashtable();
      StringTokenizer markertok = 
	  new StringTokenizer((String) attributes.get("MARKER_NAMES"));
      int ind=0;
      while (markertok.hasMoreTokens()) {
	  String mstr = markertok.nextToken();
	  mstr.trim();
	  mdata.put(mstr,data.viewPart(0,ind,data.rows(),3));
	  ind=ind+3;
      }
      
      // Test the newly created Hashtable
      if (false) {
	  System.err.println("First marker positions");
	  for (Enumeration en = mdata.keys(); en.hasMoreElements();) {
	      String k = (String) en.nextElement();
	      DoubleMatrix2D md = (DoubleMatrix2D) mdata.get(k);
	      System.err.println(k);
	      System.err.println(md.viewRow(0));
	  }
	  System.exit(1);
      }
  }

    /** Returns the set of marker names in the file */
    public Enumeration getMarkerNames() {
	return mdata.keys();
    }

    /** 
     *Returns the complete data matrix
     */
    public DoubleMatrix2D getAllData() {
	return data;
    }

    /** 
     *Returns the attributes hash
     */
    public Hashtable getAttributes() {
	return attributes;
    }

  /**
    Returns the number of columns
    */
  public int columns() {
    return data.columns();
  }

  /**
    Returns the number of rows
    */
  public int rows() {
    return data.rows();
  }


    /** Returns the value of an attribute
     */
    public String getValue(String key) {
	return (String) attributes.get(key);
    }

    /** Returns an enumeration with the attribute names
     */
    public Enumeration getKeys() {
	return attributes.keys();
    }


    /** Returns an iterator that traverses the data matrix along the 
	specified dimension. dim=0 (default) starts down the first column,
	dim=1 starts along the first row.
    */
    public doubleStream getData(int dim) throws IllegalArgumentException {
	if (dim!=0 && dim!=1) {
	    throw new IllegalArgumentException();
	}
	if (dim==0)
	    return getData();
	else
	    return new ColumnSet(0,data.columns()-1);
    }

    public doubleStream getData(){
	return new RowSet(0,data.rows()-1);
    }
    
  /** Same as above, except i rows are skipped between the rows
    returned.
    */
  public doubleStream getSparseData(int i) throws IllegalArgumentException {
    return new ColumnSet(0,data.columns()-1,i);
  }
    
  public doubleStream getColumn(int i) throws IllegalArgumentException {
    return new Column(i);
  }
    
  /** Same as above, except rows are skipped between the rows
    returned.
    */
  public doubleStream getColumn(int i, int skip) 
    throws IllegalArgumentException {
    return new Column(i,skip);
  }
    
  public doubleStream getColumnSet(int fstcol, int lstcol) 
    throws IllegalArgumentException {
    return new ColumnSet(fstcol,lstcol);
  }
    
  /** Same as above, except rows are skipped between the rows
    returned.
    */
  public doubleStream getColumnSet(int fstcol, int lstcol, int skip) 
    throws IllegalArgumentException {
    return new ColumnSet(fstcol,lstcol,skip);
  }
    
  public doubleStream getRow(int i) throws IllegalArgumentException {
    return new Row(i);
  }
    
    
    //    public DoubleMatrix2D getMarkerSetData(String markerset) {
    //	return getMarkerSetData(parseMarkerSet(markerset));
    //    }

    public DoubleMatrix2D getMarkerSetData(String[] markerset) {

	DoubleMatrix2D dataset = 
	    DoubleFactory2D.dense.make(data.rows(),markerset.length*3);
	int ind=0;
	for (int i=0; i<markerset.length; i++) {	
	    try {
		dataset.viewPart(0,ind,data.rows(),3).assign((DoubleMatrix2D)
    				 this.mdata.get(markerset[i]));
	    } catch (NullPointerException np) {
		System.err.println("No data for marker " + markerset[i]);
		System.err.println("Markers in file:");
		for (Enumeration e = mdata.keys(); e.hasMoreElements();) {
		System.err.println(e.nextElement());
		}
	    }
	    //System.err.println("index = "+ ind);
	    //System.err.println("data ");
	    //System.err.println(((DoubleMatrix2D) 
	    //this.mdata.get(markerset[i])).viewRow(0));
	    ind=ind+3;
	}

	return stripZeroes(dataset);
    }
	
    /** Strips off zeroes at the beginning and end of the data set
     */
    private static DoubleMatrix2D stripZeroes(DoubleMatrix2D data) {
	int row=0;
	while (row<data.rows() && 
	       data.viewRow(row).cardinality()<data.columns()) 
	    row++;
	int startrow = row;

	row=data.rows()-1;
	while (row>-1 && 
	       data.viewRow(row).cardinality()<data.columns()) 
	    row--;
	int endrow = row;

	if (startrow>endrow) return DoubleFactory2D.dense.make(0,0);
	else return data.viewPart( 
		      startrow,0,endrow-startrow+1,data.columns()).copy();
    }

  //---------------------------------------------------- 
  // Inner classes
  //----------------------------------------------------
  
  /** 
    This private inner class give sequential access to 
    a column.
    */
  private class Column implements doubleStream
  {
    private DoubleMatrix1D col;
    private int row=-1;
    private int skip=0;

    public Column(int st) throws IllegalArgumentException {
      if (st<0 | st>=data.columns()) {
	throw new IllegalArgumentException();
      }
      col=data.viewColumn(st);
    }

    public Column(int st, int skip) throws IllegalArgumentException {
      if (st<0 | st>data.columns()) {
	throw new IllegalArgumentException();
      }
      col=data.viewColumn(st);
      this.skip=skip;
    }

      public void reset() {
	  row = -1;
      }

    public boolean hasNext() {
      if (data==null) {
	return false;
      }
      else {
	return (row<col.size()-1);
      }
    }

    public double next() throws NoSuchElementException {
      try {
	row=row+1+skip;
	return col.get(row);
      }
      catch (Exception e) {
	throw new NoSuchElementException();
      }
    }

    public int size() {
      if (skip==0) {
	return col.size();
      }
      else {
	return (col.size())/(skip+1);
      }
    }
  }

  /** 
    This private inner class gives sequential access to 
    a given set of columns.
    */
  private class ColumnSet implements doubleStream
  {
      private DoubleMatrix2D colset;
      private int row=0;
      private int col=-1;
      private int skip=0;
      private int lastcol=0;

    public ColumnSet(int fstcol,int lstcol) throws IllegalArgumentException {
	this(fstcol,lstcol,0);
    }

    public ColumnSet(int fstcol,int lstcol, int skip) 
      throws IllegalArgumentException {
	if (fstcol<0 | lstcol>=data.columns() | fstcol>lstcol) {
	  throw new IllegalArgumentException();
	}
	colset = data.viewPart(0,fstcol,data.rows(),lstcol-fstcol+1);
	this.skip = skip;
	lastcol=lstcol-fstcol;
    }

      public void reset() {
	  row=0;
	  col=-1;
      }

    public boolean hasNext() {
      if (data==null) {
	return false;
      }
      else {
	return ((row<colset.rows()) && (col<colset.columns()-1));
      }
    }

    public double next() throws NoSuchElementException {
      double tmp;
      try {
	col++;
	tmp =  colset.get(row,col);
	//System.err.println("# TSVReader row= " + row + " col=" + col);
	if (col==lastcol) {
	    col=-1;
	    row=row+1+skip;
	}
      }
      catch (Exception e) {
	throw new NoSuchElementException();
      }
      return tmp;
    }

    public int size() {
      if (skip==0) {
	return colset.size();
      }
      else {
	return colset.columns()*(colset.rows()/(skip+1));
      }
    }
  }

  /** 
    This private inner class give sequential access to 
    a row.
    */
  private class Row implements doubleStream
  {
    private DoubleMatrix1D row;
    private int col=-1;

    public Row(int st) throws IllegalArgumentException {
      if (st<0 | st>=data.rows()) {
	throw new IllegalArgumentException();
      }
      row=data.viewRow(st);
    }

      public void reset() {
	  col=-1;
      }

    public boolean hasNext() {
      if (data==null) {
	return false;
      }
      else {
	return (col<row.size()-1);
      }
    }

    public double next() throws NoSuchElementException {
      try {
	col++;
	return row.get(col);
      }
      catch (Exception e) {
	throw new NoSuchElementException();
      }
    }

    public int size() {
      return row.size();
    }
  }

  /** 
    This private inner class gives sequential access to 
    a given set of rows. The data is accessed along the columns first
    */
  private class RowSet implements doubleStream
  {
    private DoubleMatrix2D rowset;
    private int rows;
    private int row=-1;
    private int col=0;
    private int skip=0;

    public RowSet(int fstrow,int lstrow) throws IllegalArgumentException {
	this(fstrow,lstrow,0);
    }

    public RowSet(int fstrow,int lstrow, int skip) 
      throws IllegalArgumentException {
	if (fstrow<0 | lstrow>=data.rows() | fstrow>lstrow) {
	  throw new IllegalArgumentException();
	}
	rowset = data.viewPart(fstrow,0,lstrow-fstrow+1,data.columns());
	rows = lstrow-fstrow;
	this.skip = skip;
    }

      public void reset() {
	  col=0;
	  row=-1;
      }

    public boolean hasNext() {
      if (data==null) {
	return false;
      }
      else {
	return ((col<rowset.columns()-skip) && (row<rows));
      }
    }

    public double next() throws NoSuchElementException {
      double tmp;
      try {
	row++;
	tmp =  rowset.get(row,col);
	if (row==rows) {
	    row=-1;
	    col=col+1+skip;
	}
      }
      catch (Exception e) {
	throw new NoSuchElementException();
      }
      return tmp;
    }

    public int size() {
      if (skip==0) {
	  return rowset.size();
      }
      else {
	return rowset.rows()*(rowset.columns()/(skip+1)+1);
      }
    }
  }


  static public void main(String[] args) {
      if (false) {
    try {
	URL url = new URL(args[0]);
	TSVReader tsvr = new TSVReader(url.openStream());
	doubleStream data = tsvr.getData(0);
	System.out.println("Columnwise access ");
	while (data.hasNext()) {
	    System.out.print(data.next());
	    System.out.print(',');
	}
	data = tsvr.getData(1);
	System.out.println(" ");
	System.out.println(" Rowwise access ");
	while (data.hasNext()) {
	    System.out.print(data.next());
	    System.out.print(',');
	}
	data = tsvr.getColumn(4);
	System.out.println(" ");
	System.out.println(" The fifth column ");
	while (data.hasNext()) {
	    System.out.print(data.next());
	    System.out.print(',');
	}
	data = tsvr.getRow(1);
	System.out.println(" ");
	System.out.println(" The second row ");
	while (data.hasNext()) {
	    System.out.print(data.next());
	    System.out.print(',');
	}
	
	for (Enumeration keys = tsvr.getKeys();keys.hasMoreElements();) {
	    String key = (String) keys.nextElement();
	    System.out.println("Attribute: " + key);
	    System.out.println("Value: " + tsvr.getValue(key));
	    
	}
    }
    catch (Exception e) {
	e.printStackTrace();
    }
      }
  }
}
