package com.mwendasoft.superme.music;
import java.io.*;

public class Song implements Serializable {
	private int id;
	private String title;
	private String artist;
	private String lyrics;
	
	public Song(int id, String title, String artist, String lyrics) {
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.lyrics = lyrics;
	}
	
	public int getId() {
		return id;
	}
	public String getSongTitle() {
		return title;
	}
	
	public String getSongArtist() {
		return artist;
	}
	
	public String getLyrics() {
		return lyrics;
	}
}
