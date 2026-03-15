package com.example.phongkham_app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.phongkham_app.data.local.DatabaseHelper;

public class QueueBackgroundService extends Service {

    private static final String TAG = "QueueService";
    private static final String CHANNEL_ID = "QueueChannel";
    private DatabaseHelper dbHelper;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private android.media.MediaPlayer mediaPlayer;
    private boolean hasAlerted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
        createNotificationChannel();
        try {
            mediaPlayer = android.media.MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_RINGTONE_URI);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo MediaPlayer", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, getForegroundNotification("Đang kết nối hàng đợi..."), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(1, getForegroundNotification("Đang kết nối hàng đợi..."));
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                updateQueueStatus();
                handler.postDelayed(this, 10000); // Check mỗi 10 giây cho demo
            }
        };
        handler.post(runnable);

        return START_STICKY;
    }

    private void updateQueueStatus() {
        SharedPreferences pref = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = pref.getInt("USER_ID", -1); // Khớp Key USER_ID

        if (userId == -1) return;

        Cursor appCursor = dbHelper.getLatestAppointment(userId);
        if (appCursor != null && appCursor.moveToFirst()) {
            int appIdIndex = appCursor.getColumnIndex("id");
            if (appIdIndex >= 0) {
                int appointmentId = appCursor.getInt(appIdIndex);
                
                Cursor queueCursor = dbHelper.getQueueStatus(appointmentId);
                if (queueCursor != null && queueCursor.moveToFirst()) {
                    int numIndex = queueCursor.getColumnIndex("queue_number");
                    int roomIndex = queueCursor.getColumnIndex("clinic_room_id");
                    int statusIndex = queueCursor.getColumnIndex("status");

                    String queueNumber = numIndex >= 0 ? queueCursor.getString(numIndex) : "N/A";
                    int clinicRoomId = roomIndex >= 0 ? queueCursor.getInt(roomIndex) : -1;
                    String status = statusIndex >= 0 ? queueCursor.getString(statusIndex) : "WAITING";

                    int totalWait = 0;

                    if (clinicRoomId != -1) {
                        Cursor listCursor = dbHelper.getQueueList(clinicRoomId);
                        while (listCursor != null && listCursor.moveToNext()) {
                            int statusCol = listCursor.getColumnIndex("status");
                            int durationCol = listCursor.getColumnIndex("duration_minutes");
                            int appIdCol = listCursor.getColumnIndex("appointment_id");
                            int startCol = listCursor.getColumnIndex("treatment_start_time");

                            String itemStatus = statusCol >= 0 ? listCursor.getString(statusCol) : "WAITING";
                            int duration = durationCol >= 0 ? listCursor.getInt(durationCol) : 15;
                            int itemAppId = appIdCol >= 0 ? listCursor.getInt(appIdCol) : -1;

                            if ("IN_PROGRESS".equals(itemStatus)) {
                                String startTimeStr = startCol >= 0 ? listCursor.getString(startCol) : null;
                                if (startTimeStr != null && !startTimeStr.isEmpty()) {
                                    try {
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                                        java.util.Date startTime = sdf.parse(startTimeStr);
                                        long elapsed = new java.util.Date().getTime() - startTime.getTime();
                                        int elapsedMinutes = (int) (elapsed / (1000 * 60));
                                        totalWait += Math.max(0, duration - elapsedMinutes);
                                    } catch (Exception e) { totalWait += duration; }
                                } else { totalWait += duration; }
                            } else if ("WAITING".equals(itemStatus)) {
                                if (itemAppId == appointmentId) break;
                                totalWait += duration;
                            }
                        }
                        if (listCursor != null) listCursor.close();
                    }

                    // Broadcast tới Activity
                    Intent broadcast = new Intent("com.example.phongkham_app.QUEUE_UPDATE");
                    broadcast.putExtra("QUEUE_NUMBER", queueNumber);
                    broadcast.putExtra("WAIT_TIME", totalWait);
                    broadcast.putExtra("STATUS", status);
                    sendBroadcast(broadcast);

                    // Push Notification / Alarm
                    if (totalWait <= 3 && totalWait > 0 && !"COMPLETED".equals(status)) {
                        if (mediaPlayer != null && !hasAlerted) {
                            try { mediaPlayer.start(); hasAlerted = true; } catch (Exception e) {}
                        }
                        pushNotification("Sắp đến lượt khám!", "Hàng đợi đang rút ngắn. Chuẩn bị đến cửa phòng.");
                    } else if (totalWait > 3) {
                        hasAlerted = false;
                    }

                    NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    manager.notify(1, getForegroundNotification("Số: " + queueNumber + " - Chờ: " + totalWait + " phút (" + status + ")"));
                    
                    if ("COMPLETED".equals(status)) { stopSelf(); }
                }
                if (queueCursor != null) queueCursor.close();
            }
        }
        if (appCursor != null) appCursor.close();
    }

    private Notification getForegroundNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Hàng Đợi Phòng Khám")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void pushNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        manager.notify(2, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Smart Queue Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) handler.removeCallbacks(runnable);
        if (dbHelper != null) dbHelper.close();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
