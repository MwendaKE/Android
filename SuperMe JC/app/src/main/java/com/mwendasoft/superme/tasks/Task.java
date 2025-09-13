package com.mwendasoft.superme.tasks;
import java.io.*;

public class Task implements Serializable {
	private int id;
	private String title;
	private String sdate;
	private String stime;
	private int duration;
	private String edate;
	private int category;
	private int success;
	private String description;
	
	public Task(int id, String title, String sdate, String stime, int duration, String edate, int category, int success, String description) {
		this.id = id;
		this.title = title;
		this.sdate = sdate;
		this.stime = stime;
		this.duration = duration;
		this.edate = edate;
		this.category = category;
		this.success = success;
		this.description = description;
	}
	
	public int getId() { return id;}
	public String getTitle() { return title; }
	public String getSdate() { return sdate;}
	public String getTime() { return stime; }
	public int getDuration() { return duration;}
	public String getEdate() { return edate; }
	public int getCategoryId() { return category;}
	public int getSuccess() { return success; }
	public String getDescription() { return description; }
}
