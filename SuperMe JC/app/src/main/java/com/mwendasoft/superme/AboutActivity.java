package com.mwendasoft.superme;

import android.app.Activity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.AbsoluteSizeSpan;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.mwendasoft.superme.helpers.CustomTypefaceSpan;
import android.text.style.AlignmentSpan;
import android.text.Layout;
import android.view.*;
import android.support.v4.content.*;

public class AboutActivity extends BaseActivity {

    private Typeface angelosFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        angelosFont = Typeface.createFromAsset(getAssets(), "fonts/angelos.ttf");

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.WHITE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);
        layout.setBackgroundColor(Color.WHITE);
        layout.setGravity(Gravity.CENTER_HORIZONTAL); // Center align contents

        // Logo
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo3);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(300, 300);
        imageParams.gravity = Gravity.CENTER_HORIZONTAL;
        logo.setLayoutParams(imageParams);
        layout.addView(logo);

        // Main formatted text
        TextView textView = new TextView(this);
        textView.setTextSize(18);
        textView.setLineSpacing(8, 1.0f);
        textView.setTextColor(Color.BLACK);
        textView.setText(buildFormattedText());
        textView.setGravity(Gravity.CENTER); // Center align text inside TextView
        layout.addView(textView);

        // Horizontal separator
        View line = new View(this);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT, 4);
        lineParams.setMargins(0, 20, 0, 20);
        line.setLayoutParams(lineParams);
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.customTeal));
        layout.addView(line);

        // Creator details
        TextView creatorText = new TextView(this);
        creatorText.setTextSize(18);
        creatorText.setTextColor(Color.BLACK);
        creatorText.setLineSpacing(6, 1.0f);
        creatorText.setGravity(Gravity.CENTER); // Center align text
        creatorText.setText("About Creator:\n" +
							"Programmer Name: ERICK MWENDA NJAGI\n" +
							"Email: erickmwenda256@gmail.com\n" +
							"Phone Number: +254 07 02 623 729\n" +
							"Company Name: MwendaSoft.com\n\n");
        layout.addView(creatorText);

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    private SpannableStringBuilder buildFormattedText() {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        // Introductory quote
        int startIntro = builder.length();
        builder.append("\"SuperMe is a personal life manager designed to help you organize and reflect on your world.\"\n\n");
        builder.setSpan(new StyleSpan(Typeface.BOLD), startIntro, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), startIntro, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AbsoluteSizeSpan(20, true), startIntro, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), startIntro, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // App sections
        addSection(builder, "Books", "Keeps track of all the books you’ve read and their summaries.");
        addSection(builder, "Notes", "Stores your text, image, audio, and video notes.");
        addSection(builder, "Quotes", "Manages quotes from famous people.");
        addSection(builder, "Events", "Helps you plan and remember events with text, images, audio, and video.");
        addSection(builder, "Diary", "Captures your life experiences in text, audio, video, and pictures.");
        addSection(builder, "Tasks", "Helps you manage your to-dos with timing and images.");
        addSection(builder, "Arts", "Saves and organizes articles from websites and people.");
        addSection(builder, "Sparks", "Stores videos in categories like Tech, Comedy, Motivation, Songs, etc.");
        addSection(builder, "Music", "Manages lyrics and audio files.");
        addSection(builder, "Poems", "Keeps poems in both image and text formats.");
        addSection(builder, "Clips", "Saves short excerpts from books.");
        addSection(builder, "People", "Highlights renowned people and their work.");

        // Final quote
        int startFinal = builder.length();
        builder.append("\"SuperMe is your personal space to save, learn, and grow.\"\n\n");
        builder.setSpan(new StyleSpan(Typeface.BOLD), startFinal, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), startFinal, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AbsoluteSizeSpan(20, true), startFinal, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), startFinal, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return builder;
    }

    private void addSection(SpannableStringBuilder builder, String title, String description) {
        // Section title
        int startTitle = builder.length();
        builder.append(title + "\n");
        builder.setSpan(new StyleSpan(Typeface.BOLD), startTitle, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), startTitle, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new CustomTypefaceSpan(angelosFont), startTitle, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AbsoluteSizeSpan(20, true), startTitle, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), startTitle, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Description
        int startDesc = builder.length();
        builder.append(description + "\n\n");
        builder.setSpan(new ForegroundColorSpan(Color.parseColor("#8D4004")), startDesc, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AbsoluteSizeSpan(20, true), startDesc, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), startDesc, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}
