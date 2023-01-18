package kha.track;

import java.util.Enumeration;


public class TrackException extends Exception {
    
    private Enumeration markerset = null;
    private boolean diverged = false;

    protected TrackException(String message){
	super(message);
    }

    protected TrackException(String message, Enumeration mset) {
	super(message);
	this.markerset = mset;
    }

    protected TrackException(String message,boolean diverged) {
	super(message);
	this.diverged = true;
    }
	
    public boolean diverged(){ return diverged;}

    public boolean wrongMarkers(){return (markerset != null);}

    public Enumeration getMarkerSet(){
	return markerset;
    }
}

