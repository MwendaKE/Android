package com.mwendasoft.superme;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.TextView;

import android.widget.*;
import android.view.*;

public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the ActionBar
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Set custom ActionBar layout
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setCustomView(R.layout.custom_action_bar);

            // Make the ActionBar transparent
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Remove shadow (elevation)
            actionBar.setElevation(0);

            // Set custom font and text color for title
            TextView titleTextView = actionBar.getCustomView().findViewById(R.id.customTitle);
            Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/angelos.ttf");
            titleTextView.setTypeface(customFont, Typeface.BOLD);
            titleTextView.setTextColor(Color.BLACK);
        }
    }

    // Apply background image + white layout background for every activity
    @Override
    public void setContentView(int layoutResID) {
        // Root layout
        FrameLayout root = new FrameLayout(this);

        // Background image
        ImageView bg = new ImageView(this);
        bg.setLayoutParams(new FrameLayout.LayoutParams(
							   FrameLayout.LayoutParams.MATCH_PARENT,
							   FrameLayout.LayoutParams.MATCH_PARENT
						   ));
        bg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        bg.setImageResource(R.drawable.superme_bgimage); // your background image
        bg.setAlpha(0.1f); // dull effect

        // Inflate the child layout
        View child = getLayoutInflater().inflate(layoutResID, null);

        // ✅ Set white background for all activities
        child.setBackgroundColor(Color.WHITE);

        // Add background first, then layout
        root.addView(bg);
        root.addView(child);

        // Set the combined layout
        super.setContentView(root);
    }
}
