package com.mwendasoft.superme.events;
import java.io.*;

public class Event implements Serializable {
	private int id;
    private String title;
    private String date;
    private String time;
	private String notes;
    private String address;
    private float budget;
    private int attended;

    public Event(int id, String title, String date, String time, String notes, String address, float budget, int attended) {
        this.id = id;
		this.title = title;
        this.date = date;
        this.time = time;
		this.notes = notes;
        this.address = address;
        this.budget = budget;
        this.attended = attended;
    }

    // Optional: Getters
	public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
	public String getNotes() { return notes; }
    public String getAddress() { return address; }
    public float getBudget() { return budget; }
    public int getAttendance() { return attended; }
}
