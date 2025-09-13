package com.mwendasoft.superme.clips;

public class Clip {
	private String clip;
	private String source;
	private String writer;
	
	public Clip(String clip, String source, String writer) {
		this.clip = clip;
		this.source = source;
		this.writer = writer;
	}
	
	public String getClip() {
		return clip;
	}
	
	public String getSource() {
		return source;
	}
	
	public String getWriter() {
		return writer;
	}
}
