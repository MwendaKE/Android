package com.mwendasoft.superme.books;
import java.io.*;

public class Book implements Serializable {
	private int bookId;
    private String title;
    private String author;
	private int status;
	private String categ;

    public Book(int bookId, String title, String author, int status, String categ) {
        this.bookId = bookId;
		this.title = title;
        this.author = author;
		this.status = status;
		this.categ = categ;
		
    }
	
	public int getId() {
		return bookId;
	}

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
	
	public int getStatus() {
		return status;
	}
	
	public String getCategory() {
		return categ;
	}
}
