package com.mwendasoft.newsip.globalnews;

public class GlobalNewsItem {
    private String title;
    private String description;
	private String source;
    private String author;
    private String publishedAt;
    private String url; // In case you still want to open in browser later

    // Constructor
    public GlobalNewsItem(String title, String description, String source, String author, String publishedAt, String url) {
        this.title = title;
        this.description = description;
		this.source = source;
        this.author = author;
        this.publishedAt = publishedAt;
        this.url = url;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

	public String getSource() {
        return source;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getUrl() {
        return url;
    }

	public String getSourceAuthor() {
		return getSource() + " | " + getAuthor();
	}

    // Setters (optional, if you want to modify data later)
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	public void setSource(String source) {
        this.source = source;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

