package com.mwendasoft.superme.diaries;
import java.io.*;

public class Diary implements Serializable {
	private int id;
	private String title;
	private String mood;
	private String date;
	private String time;
	
	public Diary(int id, String title, String mood, String date, String time) {
		this.id = id;
		this.title = title;
		this.mood = mood;
		this.date = date;
		this.time = time;
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getMood() {
		return mood;
	}
	
	public String getDate() {
		return date;
	}
	
	public String getTime() {
		return time;
	}
}
