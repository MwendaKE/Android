package com.mwendasoft.newsip.newshelpers;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.view.View;
import com.mwendasoft.newsip.R;

public class LoadingDialog {

    private Activity activity;
    private Dialog dialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public void show() {
        if (dialog != null && dialog.isShowing()) return;

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        FrameLayout container = dialog.findViewById(R.id.circle_container);

        int numberOfDots = 10;
        int radius = 80; // Size of the invisible big circle in pixels
        int dotSize = 16;
		
		// Define different colors for the dots
		int[] colors = {
			0xFFFFCDD2, // Light Red 100
			0xFFEF9A9A, // Light Red 200
			0xFFE57373, // Light Red 300
			0xFFEF5350, // Red 400
			0xFFF44336, // Red 500 (Material red)
			0xFFE53935, // Red 600
			0xFFD32F2F, // Red 700
			0xFFC62828, // Red 800
			0xFFB71C1C, // Red 900
			0xFFFF5252  // Accent Red (A200)
		};

		for (int i = 0; i < numberOfDots; i++) {
			ImageView dot = new ImageView(activity);
			dot.setImageResource(R.drawable.rotating_dot_circle); // Same shape

			// Change color for each dot
			dot.setColorFilter(colors[i % colors.length]);

			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dotSize, dotSize);
			dot.setLayoutParams(params);

			// Positioning
			double angle = 2 * Math.PI * i / numberOfDots;
			float x = (float) (radius * Math.cos(angle));
			float y = (float) (radius * Math.sin(angle));
			dot.setTranslationX(x + radius);
			dot.setTranslationY(y + radius);

			container.addView(dot);
		}
		
        // Apply rotation animation to the container (the invisible big circle)
        RotateAnimation rotate = new RotateAnimation(
			0, 360,
			Animation.RELATIVE_TO_SELF, 0.5f,
			Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotate.setDuration(2000);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(activity, android.R.interpolator.linear);

        container.startAnimation(rotate);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
