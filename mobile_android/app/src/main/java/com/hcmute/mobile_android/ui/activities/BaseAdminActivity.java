package com.hcmute.mobile_android.ui.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButton;
import com.airbnb.lottie.LottieAnimationView;
import com.hcmute.mobile_android.R;

public abstract class BaseAdminActivity extends AppCompatActivity {

    private Dialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLoadingDialog();
    }

    private void initLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.dialog_loading);
        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        loadingDialog.setCancelable(false);
    }

    public void showLoading(boolean show) {
        showLoading(show, "Đang xử lý...");
    }

    public void showLoading(boolean show, String message) {
        if (isFinishing()) return;
        
        if (show) {
            TextView tvMessage = loadingDialog.findViewById(R.id.tvMessage);
            if (tvMessage != null) {
                tvMessage.setText(message);
            }
            if (!loadingDialog.isShowing()) {
                loadingDialog.show();
            }
        } else {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }

    public void showSuccess(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(getResources().getColor(R.color.admin_success));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void showError(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.admin_error));
        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void updateEmptyState(boolean isEmpty, @Nullable String title, @Nullable String message) {
        updateEmptyState(isEmpty, title, message, null);
    }

    public void updateEmptyState(boolean isEmpty, @Nullable String title, @Nullable String message, @Nullable Runnable retryAction) {
        View emptyStateView = findViewById(R.id.emptyStateView);
        if (emptyStateView == null) return;

        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        
        if (isEmpty) {
            TextView tvTitle = emptyStateView.findViewById(R.id.tvEmptyTitle);
            TextView tvMessage = emptyStateView.findViewById(R.id.tvEmptyMessage);
            LottieAnimationView lottieEmpty = emptyStateView.findViewById(R.id.lottieEmpty);
            MaterialButton btnRetry = emptyStateView.findViewById(R.id.btnRetry);

            if (tvTitle != null && title != null) tvTitle.setText(title);
            if (tvMessage != null && message != null) tvMessage.setText(message);
            
            if (lottieEmpty != null) {
                lottieEmpty.playAnimation();
            }

            if (btnRetry != null) {
                if (retryAction != null) {
                    btnRetry.setVisibility(View.VISIBLE);
                    btnRetry.setOnClickListener(v -> retryAction.run());
                } else {
                    btnRetry.setVisibility(View.GONE);
                }
            }
        } else {
            LottieAnimationView lottieEmpty = emptyStateView.findViewById(R.id.lottieEmpty);
            if (lottieEmpty != null) {
                lottieEmpty.cancelAnimation();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}
