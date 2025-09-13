package com.mwendasoft.superme.people;
import org.apache.http.*;
import java.io.*;

public class Personn implements Serializable {
	private int id;
	private String name;
	private String occupation;
	private String description;

	public Personn(int id, String name, String occupation, String description) {
		this.id = id;
		this.name = name;
		this.occupation = occupation;
		this.description = description;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getOccupation() {
		return occupation;
	}

	public String getDetails() {
		return description;
	}
}
