package com.mwendasoft.superme.core;
import java.text.*;
import java.util.*;
import java.time.*;
import java.time.temporal.*;
import java.time.format.*;

public class SuperMeAppHelper {
	private String rawDate;
	private String rawTime;
	
	public SuperMeAppHelper(String date, String time) {
		this.rawDate = date;
		this.rawTime = time;
	}
	
	public String getFormattedDate() {
		// Returns something like: 1 March 2025
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat formattedDate = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());

        try {
            Date date = dbFormat.parse(rawDate);
            return (date != null) ? formattedDate.format(date) : rawDate;
        } catch (ParseException e) {
            e.printStackTrace();
            return rawDate;
        }
    }

    public String getFormattedTime() {
		// Returns something like: 12:43 Hrs
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HHmm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm 'Hrs'", Locale.getDefault());

            Date parsedTime = inputFormat.parse(rawTime);
            return (parsedTime != null) ? outputFormat.format(parsedTime) : rawTime;
        } catch (ParseException e) {
            e.printStackTrace();
            return rawTime; 
        }
    }

	public String getRelativeTime() {
		// Returns something like: 2 Months ago or 1 Dag ago.
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = dbFormat.parse(rawDate);
            if (date == null) return "";

            Calendar entryCal = Calendar.getInstance();
            entryCal.setTime(date);
            Calendar now = Calendar.getInstance();

            long diffInMillis = now.getTimeInMillis() - entryCal.getTimeInMillis();
            long days = diffInMillis / (1000 * 60 * 60 * 24);

            if (days == 0) return "Today";
            else if (days == 1) return "Yesterday";
            else if (days < 30) return days + " Days Ago";
            else if (days < 365) return (days / 30) + " Months Ago";
            else return (days / 365) + " Years Ago";
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
		}
	}
	
	public String getDueTime(String dateStr, String timeStr) {
		try {
			// Parse date
			LocalDate date = LocalDate.parse(dateStr); // expects "yyyy-MM-dd"

			// Parse time manually from "HHmm"
			int hour = Integer.parseInt(timeStr.substring(0, 2));
			int minute = Integer.parseInt(timeStr.substring(2, 4));
			LocalTime time = LocalTime.of(hour, minute);

			// Combine date and time
			LocalDateTime eventDateTime = LocalDateTime.of(date, time);

			// Get current time
			LocalDateTime now = LocalDateTime.now();

			if (now.isAfter(eventDateTime)) {
				return "This event has already passed!";
			} else {
				// Calculate how many days difference
				long daysBetween = ChronoUnit.DAYS.between(now.toLocalDate(), eventDateTime.toLocalDate());

				// Format time part like "1000 Hrs"
				String formattedTime = String.format("%02d:%02d", hour, minute) + " hrs";

				if (daysBetween == 0) {
					return "Today " + formattedTime;
				} else if (daysBetween == 1) {
					return "Tomorrow " + formattedTime;
				} else {
					// Format the date nicely
					DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
					String formattedDate = eventDateTime.format(dateFormatter);

					return formattedDate + ", " + formattedTime + " (In " + daysBetween + " days)";
				}
			}
		} catch (Exception e) {
			return "Invalid input format.";
		}
	}
}
