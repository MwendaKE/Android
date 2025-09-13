package com.mwendasoft.newsip;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
		updateTabHighlighting(R.id.navAbout);
		
        showHomeIcon(MainActivity.class); // Optional: show Home icon
    }
}
