package com.mwendasoft.superme;
import java.io.*;

public class SearchItem implements Serializable {
	private String primaryText;
	private String secondaryText;
	private String tertiaryText;
	
	public SearchItem(String text1, String text2, String text3) {
		this.primaryText = text1;
		this.secondaryText = text2;
		this.tertiaryText = text3;
	}
	
	public String getText1() {
		return primaryText;
	}
	
	public String getText2() {
		return secondaryText;
	}
	
	public String getText3() {
		return tertiaryText;
	}
}
