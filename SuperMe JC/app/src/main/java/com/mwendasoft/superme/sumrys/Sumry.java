package com.mwendasoft.superme.sumrys;

import java.io.*;

public class Sumry implements Serializable {
	private int id;
	private String title;
	private String author;
	private int favorite;
	private String sumry;

	public Sumry(int id, String title, String author, String sumry, int favourite) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.sumry = sumry;
		this.favorite = favourite;
	}

	public int getId() { return id;}
	public String getTitle() { return title; }
	public String getAuthor() { return author;}
	public String getSumry() { return sumry; }
	public int getFavorite() { return favorite;}
	public void setFavorite() {this.favorite = 1;}
	public void unSetFavorite() {this.favorite = 0;}
}
