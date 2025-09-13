package com.mwendasoft.superme.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mwendasoft.superme.R;

public class ToastMessagesManager extends Toast {

    public ToastMessagesManager(Context context) {
        super(context);
    }

    // Static method to show a toast with app icon
    public static void show(Context context, String message) {
        // Inflate custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.toast_messages_manager, null);

        // Set icon (you can change the resource if needed)
        ImageView icon = layout.findViewById(R.id.toast_icon);
        icon.setImageResource(R.drawable.ic_launcher_white); // app icon

        // Set message text
        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        // Create and show toast
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
