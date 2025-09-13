package com.mwendasoft.superme.core;
import java.text.*;
import java.util.*;
import android.os.*;
import android.widget.*;
import android.text.*;
import android.text.style.*;
import android.support.v4.content.*;
import com.mwendasoft.superme.R;
import android.graphics.*;

public class SuperMeDateHelper {
	public static String convertDays(int days) {
		if (days < 7) {
			return days + " day" + (days == 1 ? "" : "s");
		} else if (days < 30) {
			int weeks = days / 7;
			return weeks + " week" + (weeks == 1 ? "" : "s");
		} else if (days < 365) {
			int months = days / 30;
			return months + " month" + (months == 1 ? "" : "s");
		} else {
			int years = days / 365;
			return years + " year" + (years == 1 ? "" : "s");
		}
	}
	
	public static String getShortDeadline(String startDateStr, String endDateStr) {
		// This method gives the normal time as: 4d 20h 23m; (No live countdown)
		
		try {
			SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			SimpleDateFormat endFormat = new SimpleDateFormat("yyyy-MM-dd HHmm", Locale.getDefault());

			Date startDate = startFormat.parse(startDateStr);
			Date endDate = endFormat.parse(endDateStr);
			Date now = new Date();

			if (endDate.before(now)) {
				return "Deadline: Expired!";
			}

			long diffMillis = endDate.getTime() - now.getTime();

			long minutes = diffMillis / (60 * 1000) % 60;
			long hours = diffMillis / (60 * 60 * 1000) % 24;
			long days = diffMillis / (24 * 60 * 60 * 1000);

			StringBuilder result = new StringBuilder("Deadline: ");
			if (days > 0) result.append(days).append("d ");
			if (hours > 0) result.append(hours).append("h ");
			if (minutes > 0) result.append(minutes).append("m ");
			if (days == 0 && hours == 0 && minutes == 0) result.append("0m ");

			result.append("from now.");
			return result.toString().trim();

		} catch (Exception e) {
			e.printStackTrace();
			return "Deadline: Invalid date!";
		}
	}
	
	public static void startCountdownMinute(String startDateStr, String endDateStr, int success, final TextView textView) {
		// This method gives live time countdown as: 2d 3h 20m (Doesnt include second)
	    try {
			SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			SimpleDateFormat endFormat = new SimpleDateFormat("yyyy-MM-dd HHmm", Locale.getDefault());

			Date startDate = startFormat.parse(startDateStr);
			Date endDate = endFormat.parse(endDateStr);
			Date now = new Date();

			// STYLE THE DEADLINE PART
			
			if (endDate.before(now)) {
				if (success == 1) {
					SpannableString completed = new SpannableString("Deadline: Completed!");

					// Make "Deadline:" green 
					completed.setSpan(new ForegroundColorSpan(Color.parseColor("#0B6623")), 0, "Deadline:".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					// Make "Task completed!" green and italic
					completed.setSpan(new ForegroundColorSpan(Color.parseColor("#0B6623")), "Deadline: ".length(), completed.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					completed.setSpan(new StyleSpan(Typeface.ITALIC), "Deadline: ".length(), completed.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					textView.setText(completed);
				} else {
					SpannableString expired = new SpannableString("Deadline: Expired!");

					// Make "Deadline:" green
					expired.setSpan(new ForegroundColorSpan(Color.parseColor("#800000")), 0, "Deadline: ".length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					// Make "Task completed!" maroon and italic
					expired.setSpan(new ForegroundColorSpan(Color.parseColor("#800000")), "Deadline: ".length(), expired.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					expired.setSpan(new StyleSpan(Typeface.ITALIC), "Deadline: ".length(), expired.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					textView.setText(expired);
				}
				return;
			}
			
			// END
			
			long duration = endDate.getTime() - now.getTime();

			new CountDownTimer(duration, 1000) { // every second
				public void onTick(long millisUntilFinished) {
					long minutes = millisUntilFinished / (60 * 1000) % 60;
					long hours = millisUntilFinished / (60 * 60 * 1000) % 24;
					long days = millisUntilFinished / (24 * 60 * 60 * 1000);

					SpannableStringBuilder builder = new SpannableStringBuilder();
					
					// "Deadline: " in black
					String prefix = "Deadline: ";
					builder.append(prefix);
					builder.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					// Countdown (e.g., 2d 3h 4m) in maroon and bold
					StringBuilder timePart = new StringBuilder();
					if (days > 0) timePart.append(days).append("d ");
					if (hours > 0) timePart.append(hours).append("h ");
					if (minutes > 0) timePart.append(minutes).append("m ");
					if (days == 0 && hours == 0 && minutes == 0) timePart.append("0m ");

					int startCountdown = builder.length();
					builder.append(timePart.toString());
					int endCountdown = builder.length();

					builder.setSpan(new ForegroundColorSpan(Color.parseColor("#800000")), startCountdown, endCountdown, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					builder.setSpan(new StyleSpan(Typeface.BOLD), startCountdown, endCountdown, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					// "from now" in black and italic
					String suffix = " from now";
					int startSuffix = builder.length();
					builder.append(suffix);
					int endSuffix = builder.length();

					builder.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), startSuffix, endSuffix, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					builder.setSpan(new StyleSpan(Typeface.ITALIC), startSuffix, endSuffix, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					textView.setText(builder);
				}

				public void onFinish() {
					textView.setText("Deadline: Expired!");
				}
			}.start();

		} catch (Exception e) {
			e.printStackTrace();
			textView.setText("Deadline: Invalid date!");
		}
	}
	
	public static void startCountdownSecond(String startDateStr, String endDateStr, final TextView textView) {
		// This method gives live time countdown as: 2d 3h 20m 10s (Includes seconds)
		
		try {
			SimpleDateFormat startFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			SimpleDateFormat endFormat = new SimpleDateFormat("yyyy-MM-dd HHmm", Locale.getDefault());

			Date startDate = startFormat.parse(startDateStr);
			Date endDate = endFormat.parse(endDateStr);
			Date now = new Date();

			if (endDate.before(now)) {
				textView.setText("Deadline: Expired!");
				return;
			}
			
			long duration = endDate.getTime() - now.getTime();

			new CountDownTimer(duration, 1000) { // Count down every second
				public void onTick(long millisUntilFinished) {
					long seconds = (millisUntilFinished / 1000) % 60;
					long minutes = (millisUntilFinished / (60 * 1000)) % 60;
					long hours = (millisUntilFinished / (60 * 60 * 1000)) % 24;
					long days = millisUntilFinished / (24 * 60 * 60 * 1000);

					StringBuilder result = new StringBuilder("Deadline: ");
					if (days > 0) result.append(days).append("d ");
					if (hours > 0) result.append(hours).append("h ");
					if (minutes > 0) result.append(minutes).append("m ");
					result.append(seconds).append("s from now.");

					textView.setText(result.toString().trim());
				}

				public void onFinish() {
					textView.setText("Deadline: Expired!");
				}
			}.start();

		} catch (Exception e) {
			e.printStackTrace();
			textView.setText("Deadline: Invalid date!");
		}
	}
	
	public static boolean isTimeInPast(String timeStr) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HHmm", Locale.getDefault());
			Date givenTime = format.parse(timeStr);
			Date now = new Date();

			return givenTime.before(now);
		} catch (ParseException e) {
			e.printStackTrace();
			return false; // Or handle invalid format as needed
		}
	}
}
