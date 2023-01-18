/**
 * Represents a test subject.
 *
 * @author   Kjartan Halvorsen
 * @version  1.0  2003-04-09
 */

package kha.hm;


public class Subject{
    
    private int id;
    private String firstname;
    private String lastname;
    private String gender;
    private int yearOfBirth;
    private int debutYear;

    public Subject() {
	id = -1;
	firstname = "";
	lastname = "";
	gender = "";
    }

    public Subject(int id, String fname, String lname, 
		   int year, String gender, int dyear) {
	this(id, fname, lname);
	this.yearOfBirth = year;
	this.gender = gender;
	this.debutYear = dyear;
    }
    
    public Subject(int id, String fname, String lname) {
	this.id = id;
	this.firstname = fname;
	this.lastname = lname;
	this.yearOfBirth = 0;
	this.debutYear = 0;
	this.gender = "male";
    }

    public Subject(int id, String fname, String lname, String gender) {
	this.id = id;
	this.firstname = fname;
	this.lastname = lname;
	this.yearOfBirth = 0;
	this.debutYear = 0;
	this.gender = gender;
    }

    public int getID() {
	return id;
    }

    public boolean isMale() {
	return this.gender.equalsIgnoreCase("male");
    }

    public String getFirstName() {
	return this.firstname;
    }

    public String getLastName() {
	return this.lastname;
    }

    public int getYearOfBirth() {
	return this.yearOfBirth;
    }

    public int getDebutYear() {
	return this.debutYear;
    }

    
    public String toString() {
	if (this.id < 0) {
	    return "";
	} else {
	    String yb;
	    if (this.yearOfBirth == 0) {
		yb = "";
	    } else {
		yb = "  " + Integer.toString(this.yearOfBirth);
	    }

	    return "Subject: " + Integer.toString(this.id) 
		+ "   " + firstname + " " + lastname
		+ yb + "  " + gender;
	}
    }

    public String toListString() {
	return this.toString();
	/*
	  return firstname + " " + lastname + " " + yearOfBirth + " "
	  + gender;
	*/
    }

}

