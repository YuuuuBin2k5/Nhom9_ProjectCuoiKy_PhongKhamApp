package com.hcmute.mobile_android.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.hcmute.mobile_android.R;

public class ToastUtils {
    public static void showCenteredToast(Context context, String message) {
        showCustomCenteredToast(context, message, 2000);
    }

    public static void showCenteredToastLong(Context context, String message) {
        showCustomCenteredToast(context, message, 3500);
    }

    private static void showCustomCenteredToast(Context context, String message, int durationMs) {
        if (context == null || message == null) return;
        
        // Try to get Activity from context
        android.app.Activity activity = null;
        Context current = context;
        while (current instanceof android.content.ContextWrapper) {
            if (current instanceof android.app.Activity) {
                activity = (android.app.Activity) current;
                break;
            }
            current = ((android.content.ContextWrapper) current).getBaseContext();
        }
        
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // Fallback to standard toast if no valid activity is found
            try {
                android.widget.Toast.makeText(context, message, 
                    durationMs > 2200 ? android.widget.Toast.LENGTH_LONG : android.widget.Toast.LENGTH_SHORT).show();
            } catch (Exception e) {}
            return;
        }
        
        final android.app.Activity finalActivity = activity;
        new Handler(Looper.getMainLooper()).post(() -> {
            try {
                LayoutInflater inflater = LayoutInflater.from(finalActivity);
                View layout = inflater.inflate(R.layout.layout_custom_toast, null);
                
                TextView text = layout.findViewById(R.id.toast_text);
                text.setText(message);
                
                final PopupWindow popupWindow = new PopupWindow(
                    layout, 
                    ViewGroup.LayoutParams.WRAP_CONTENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT, 
                    false
                );
                
                // Show at center
                View decorView = finalActivity.getWindow().getDecorView();
                popupWindow.showAtLocation(decorView, Gravity.CENTER, 0, 0);
                
                // Auto dismiss
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        if (popupWindow.isShowing() && !finalActivity.isFinishing()) {
                            popupWindow.dismiss();
                        }
                    } catch (Exception e) {}
                }, durationMs);
            } catch (Exception e) {}
        });
    }
}
