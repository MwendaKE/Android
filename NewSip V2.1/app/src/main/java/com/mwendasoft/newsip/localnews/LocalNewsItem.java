package com.mwendasoft.newsip.localnews;

public class LocalNewsItem {

    private String title;
    private String link;
    private String description;
    private String pubDate;
    private String source;

    public LocalNewsItem(String title, String link, String description, String pubDate, String source) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.pubDate = pubDate;
        this.source = source;
    }

    public String getTitle() { return title; }
    public String getLink() { return link; }
    public String getDescription() { return description; }
    public String getPubDate() { return pubDate; }
    public String getSource() { return source; }
}
