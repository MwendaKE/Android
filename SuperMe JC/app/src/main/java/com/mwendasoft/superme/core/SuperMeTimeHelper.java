package com.mwendasoft.superme.core;
import java.text.*;
import java.util.*;
import java.time.*;
import java.time.temporal.*;
import java.time.format.*;

public class SuperMeTimeHelper {
	private String rawDate;
	private String rawTime;

	public SuperMeTimeHelper(String date, String time) {
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
		// Handles both past and future time
		SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		try {
			Date date = dbFormat.parse(rawDate);
			if (date == null) return "";

			// Strip time from both dates
			Calendar entryCal = Calendar.getInstance();
			entryCal.setTime(date);
			entryCal.set(Calendar.HOUR_OF_DAY, 0);
			entryCal.set(Calendar.MINUTE, 0);
			entryCal.set(Calendar.SECOND, 0);
			entryCal.set(Calendar.MILLISECOND, 0);

			Calendar now = Calendar.getInstance();
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);
			now.set(Calendar.MILLISECOND, 0);

			long diffInMillis = entryCal.getTimeInMillis() - now.getTimeInMillis();
			boolean isFuture = diffInMillis > 0;
			long days = Math.abs(diffInMillis) / (1000 * 60 * 60 * 24);

			String suffix = isFuture ? " from now" : " ago";

			if (days == 0) return "Today";
			else if (days == 1) return isFuture ? "Tomorrow" : "Yesterday";
			else if (days < 7) return days + " Days" + suffix;
			else if (days < 30) return (days / 7) + " Weeks" + suffix;
			else if (days < 365) return (days / 30) + " Months" + suffix;
			else return (days / 365) + " Years" + suffix;
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public String getDueTime() {
		try {
			// Parse date and time
			LocalDate date = LocalDate.parse(rawDate); // Format: yyyy-MM-dd
			int hour = Integer.parseInt(rawTime.substring(0, 2));
			int minute = Integer.parseInt(rawTime.substring(2, 4));
			LocalTime time = LocalTime.of(hour, minute);
			LocalDateTime eventDateTime = LocalDateTime.of(date, time);

			// Current time
			LocalDateTime now = LocalDateTime.now();

			if (now.isAfter(eventDateTime)) {
				return "This event already passed!";
			}

			long daysBetween = ChronoUnit.DAYS.between(now.toLocalDate(), eventDateTime.toLocalDate());
			String formattedTime = String.format("%02d:%02d", hour, minute) + " hrs";
			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
			String formattedDate = eventDateTime.format(dateFormatter);

			if (daysBetween == 0) {
				return "Today at " + formattedTime;
			} else if (daysBetween == 1) {
				return "Tomorrow at " + formattedTime;
			}

			String relativeTime;
			if (daysBetween < 7) {
				relativeTime = daysBetween + " days from now";
			} else if (daysBetween < 30) {
				long weeks = daysBetween / 7;
				String weekWord = (weeks == 1) ? "week" : "weeks";
				relativeTime = weeks + " " + weekWord + " from now";
			} else if (daysBetween < 365) {
				long months = daysBetween / 30;
				String monthWord = (months == 1) ? "month" : "months";
				relativeTime = months + " " + monthWord + " from now";
			} else {
				long years = daysBetween / 365;
				String yearWord = (years == 1) ? "year" : "years";
				relativeTime = years + " " + yearWord + " from now";
			}

			return formattedDate + " at " + formattedTime + " (" + relativeTime + ")";
		} catch (Exception e) {
			return "Invalid input format.";
		}
	}
}
