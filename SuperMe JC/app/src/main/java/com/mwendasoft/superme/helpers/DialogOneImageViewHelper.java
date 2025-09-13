package com.mwendasoft.superme.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mwendasoft.superme.R;
import android.widget.*;

public class DialogOneImageViewHelper {
    private AlertDialog dialog;

    public void showImageDialog(Context context, String imagePath) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_one_image_view_helper, null);

        // Initialize views
        ImageView imageView = dialogView.findViewById(R.id.imageDialogView);
        ImageButton btnClose = dialogView.findViewById(R.id.btnClose);

        // Load the image with error handling
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                // If image fails to load, close dialog and show toast
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show();
            return;
        }

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
}
