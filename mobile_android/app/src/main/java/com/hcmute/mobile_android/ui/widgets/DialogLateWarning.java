package com.hcmute.mobile_android.ui.widgets;

import android.content.Context;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogLateWarning {

    public interface LateActionCallback {
        void onConvertToWalkin();
        void onCancel();
    }

    public static void show(Context context, String patientName, int lateMinutes, LateActionCallback callback) {
        new MaterialAlertDialogBuilder(context)
            .setTitle("Cảnh báo: Bệnh nhân đến trễ!")
            .setMessage("Bệnh nhân " + patientName + " đã trễ " + lateMinutes + " phút so với lịch hẹn.\n\n" +
                        "Theo quy định (>15 phút), hệ thống sẽ nhường slot hiện tại cho khách ưu tiên/khách tại sảnh và chuyển bệnh nhân này sang diện vãng lai (Walk-in).\n\nBạn có đồng ý chuyển trạng thái?")
            .setPositiveButton("Chuyển Walk-in", (dialog, which) -> {
                if (callback != null) callback.onConvertToWalkin();
            })
            .setNegativeButton("Hủy bỏ", (dialog, which) -> {
                if (callback != null) callback.onCancel();
            })
            .setCancelable(false)
            .show();
    }
}
