package com.mwendasoft.superme.authors;

public class Author {
	private int authorId;
	private String authorName;
	private String authorOccupation;
	
	public Author(int id, String name, String occupation) {
		this.authorId = id;
		this.authorName = name;
		this.authorOccupation = occupation;
	}
	
	public int getId() {
		return authorId;
	}
	
	public String getName() {
		return authorName;
	}
	
	public String getOccupation() {
		return authorOccupation;
	}
}
