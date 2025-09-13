package com.mwendasoft.newsip.bookmarks; 

public class BookmarkItem {
    private String title;
    private String url;

    public BookmarkItem(String title, String url) {
        this.title = title;
        this.url = url;
    }

    // Getter for title
    public String getTitle() {
        return title;
    }

    // Setter for title
    public void setTitle(String title) {
        this.title = title;
    }

    // Getter for URL
    public String getUrl() {
        return url;
    }

    // Setter for URL
    public void setUrl(String url) {
        this.url = url;
    }
}
