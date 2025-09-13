package com.mwendasoft.superme.articles;
import java.io.*;

public class Article implements Serializable {
	private int id;
	private  String articleTitle;
	private String articleWriter;
	private int categId;
	
	public Article(int id, String title, String writer, int categId) {
		this.id = id;
		this.articleTitle = title;
		this.articleWriter = writer;
		this.categId = categId;
	}
	
	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return articleTitle;
	}
	
	public String getWriter() {
		return articleWriter;
	}
	
	public int getCategId() {
		return categId;
	}
}
