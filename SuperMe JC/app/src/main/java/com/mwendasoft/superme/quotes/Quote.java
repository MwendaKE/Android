package com.mwendasoft.superme.quotes;

public class Quote {
	private int quoteId;
	private String quoteText;
	private int authorId;
	private String authorName;
	
	public Quote(int id, String quoteText, int authorId, String authorName) {
		this.quoteId = id;
		this.quoteText = quoteText;
		this.authorId = authorId;
		this.authorName = authorName;
	}
	
	public String getQuoteText() {
		return quoteText;
	}
	
	public int getAuthorId() {
		return authorId;
	}
	
	public String getAuthorName() {
		return authorName;
	}
	
	public int getId() {
		return quoteId;
	}
}
