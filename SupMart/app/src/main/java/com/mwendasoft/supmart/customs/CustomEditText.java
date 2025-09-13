package com.mwendasoft.supmart.customs;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;
import android.graphics.Color;

import com.mwendasoft.supmart.R;

public class CustomEditText extends EditText {

    public CustomEditText(Context context) {
        super(context);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Customize your EditText here
        setTextColor(Color.BLACK); // Text color
        setHintTextColor(Color.GRAY); // Hint color
        setTextSize(16); // Text size in sp
        setTypeface(Typeface.SANS_SERIF, Typeface.BOLD); // Font style
        setPadding(20, 20, 20, 20); // Padding in pixels

        // Optional: Add a background or border
        setBackgroundResource(R.drawable.editext_border);
    }
}
