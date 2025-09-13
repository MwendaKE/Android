package com.mwendasoft.bittowl;

public class SmsEntry {
    public String type;     // "Sent" or "Received"
    public String address;  // Phone number
    public String body;     // Message content
    public long date;       // Time in milliseconds

    // Optional constructor (not required, but useful)
    public SmsEntry(String type, String address, String body, long date) {
        this.type = type;
        this.address = address;
        this.body = body;
        this.date = date;
    }
}
