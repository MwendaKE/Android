package com.mwendasoft.superme.notes;
import java.io.*;

public class Note implements Serializable {
	private int noteId;
	private String title;
	private int important;
	private String noteText;
	
	public Note(int id, String title, int important, String text) {
		this.noteId = id;
		this.title = title;
		this.important = important;
		this.noteText = text;
	}
	
	public int getId() { return noteId;}
	public int getImportance() {return important;}
	public String getTitle() {return title;}
	public String getText() {return noteText;}
}
