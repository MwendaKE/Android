package com.mwendasoft.superme.poems;
import java.io.*;

public class Poem implements Serializable {
	private int id;
	private String poemTitle;
	private String poemAuthor;
	
	public Poem(int id, String title, String author) {
		this.id = id;
		this.poemTitle = title;
		this.poemAuthor = author;
	}
	
	public int getId() {
		return id;
	}
	
	public String getPoemTitle() {
		return poemTitle;
	}
	
	public String getPoemAuthor() {
		return poemAuthor;
	}
}
