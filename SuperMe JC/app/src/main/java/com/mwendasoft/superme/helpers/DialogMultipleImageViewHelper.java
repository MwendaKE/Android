package com.mwendasoft.superme.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.view.GestureDetector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mwendasoft.superme.R;

public class DialogMultipleImageViewHelper {
    private Context context;
    private List<String> imagePaths;
    private AlertDialog dialog;
    private ImageView imageView;
    private ImageButton btnPrevious, btnNext, btnClose;
    private int currentIndex;
    private GestureDetector gestureDetector;

    public DialogMultipleImageViewHelper(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    public void showImageDialog(String currentImagePath) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_multiple_image_view_helper, null);

        // Initialize views
        imageView = dialogView.findViewById(R.id.imageDialogView);
        btnPrevious = dialogView.findViewById(R.id.btnPrevious);
        btnNext = dialogView.findViewById(R.id.btnNext);
        btnClose = dialogView.findViewById(R.id.btnClose);

        // List of all buttons
        final List<ImageButton> buttons = Arrays.asList(btnClose, btnPrevious, btnNext);

        // Set initial image
        currentIndex = imagePaths.indexOf(currentImagePath);
        if (currentIndex == -1) currentIndex = 0;
        updateImage(imagePaths.get(currentIndex));

        // Gesture detector setup
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onDoubleTap(MotionEvent e) {
					if (imageView.getScaleType() == ImageView.ScaleType.FIT_CENTER) {
						imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					} else {
						imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
					}
					return true;
				}

				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					boolean shouldHide = btnClose.getVisibility() == View.VISIBLE;
					toggleButtonsVisibility(buttons, shouldHide);
					return true;
				}
			});

        // Image touch handling
        imageView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gestureDetector.onTouchEvent(event);
					return true;
				}
			});

        // Button click listeners
        final View.OnClickListener keepButtonsVisible = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnClose.getVisibility() != View.VISIBLE) {
                    toggleButtonsVisibility(buttons, false);
                }
            }
        };

        btnPrevious.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					keepButtonsVisible.onClick(v);
					if (currentIndex > 0) {
						currentIndex--;
						updateImage(imagePaths.get(currentIndex));
						updateButtonStates();
					}
				}
			});

        btnNext.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					keepButtonsVisible.onClick(v);
					if (currentIndex < imagePaths.size() - 1) {
						currentIndex++;
						updateImage(imagePaths.get(currentIndex));
						updateButtonStates();
					}
				}
			});

        // Initial button states
        updateButtonStates();
        toggleButtonsVisibility(buttons, false);

        // Create and show dialog
        dialog = new AlertDialog.Builder(context)
			.setView(dialogView)
			.setCancelable(true)
			.create();

        btnClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			});

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void updateImage(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    private void updateButtonStates() {
        btnPrevious.setEnabled(currentIndex > 0);
        btnNext.setEnabled(currentIndex < imagePaths.size() - 1);

        btnPrevious.setAlpha(currentIndex > 0 ? 1.0f : 0.5f);
        btnNext.setAlpha(currentIndex < imagePaths.size() - 1 ? 1.0f : 0.5f);
    }

    private void toggleButtonsVisibility(List<ImageButton> buttons, boolean shouldHide) {
        for (final ImageButton button : buttons) {
            if (shouldHide) {
                button.animate()
					.alpha(0f)
					.setDuration(200)
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							button.setVisibility(View.GONE);
						}
					});
            } else {
                button.setAlpha(0f);
                button.setVisibility(View.VISIBLE);
                button.animate()
					.alpha(1f)
					.setDuration(200);
            }
        }
    }
}
