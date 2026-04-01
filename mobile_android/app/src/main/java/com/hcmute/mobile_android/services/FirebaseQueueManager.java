package com.hcmute.mobile_android.services;

import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hcmute.mobile_android.network.models.QueueItem;

import java.util.ArrayList;
import java.util.List;

public class FirebaseQueueManager {
    private static final String TAG = "FirebaseQueueManager";
    private DatabaseReference queueRef;
    private ValueEventListener queueListener;
    private QueueUpdateListener updateListener;

    public interface QueueUpdateListener {
        void onQueueUpdated(List<QueueItem> items);
    }

    public FirebaseQueueManager(Long roomId, QueueUpdateListener listener) {
        this.updateListener = listener;
        try {
            // Lắng nghe nhánh dữ liệu của phòng khám tương ứng
            queueRef = FirebaseDatabase.getInstance().getReference("clinic").child("rooms").child(String.valueOf(roomId)).child("queue");
        } catch (Exception e) {
            Log.w(TAG, "Firebase not configured, real-time updates disabled: " + e.getMessage());
            queueRef = null;
        }
    }

    public void startListening() {
        if (queueRef == null) {
            Log.w(TAG, "Firebase not configured, cannot start listening");
            return;
        }
        if (queueListener == null) {
            queueListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<QueueItem> list = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        QueueItem item = child.getValue(QueueItem.class);
                        if (item != null) {
                            list.add(item);
                        }
                    }
                    if (updateListener != null) {
                        updateListener.onQueueUpdated(list);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Lỗi đọc dữ liệu hàng đợi từ Firebase: " + error.getMessage());
                }
            };
            queueRef.addValueEventListener(queueListener);
        }
    }

    public void stopListening() {
        if (queueRef != null && queueListener != null) {
            queueRef.removeEventListener(queueListener);
            queueListener = null;
        }
    }
}
