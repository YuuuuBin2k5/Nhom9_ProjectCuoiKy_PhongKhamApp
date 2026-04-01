package com.hcmute.mobile_android.ui.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButton;
import com.hcmute.mobile_android.R;

import android.widget.Filterable;
import androidx.appcompat.widget.SearchView;
import com.google.android.material.appbar.MaterialToolbar;

public abstract class BaseAdminActivity extends AppCompatActivity {

    private Dialog loadingDialog;

    public void setupSearch(MaterialToolbar toolbar, Filterable adapter) {
        if (toolbar == null || adapter == null) return;
        
        toolbar.inflateMenu(R.menu.menu_admin_search);
        android.view.MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Tìm kiếm...");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    adapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    return false;
                }
            });
        }
    }

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

    public void showErrorDialog(String title, String message) {
        if (isFinishing()) return;
        new android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đóng", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    protected String parseErrorBody(retrofit2.Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                com.google.gson.JsonObject json = new com.google.gson.Gson().fromJson(errorJson, com.google.gson.JsonObject.class);
                if (json.has("message")) {
                    return json.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {}
        return "Lỗi hệ thống (" + response.code() + ")";
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
            ImageView ivEmptyFallback = emptyStateView.findViewById(R.id.ivEmptyFallback);
            MaterialButton btnRetry = emptyStateView.findViewById(R.id.btnRetry);

            if (tvTitle != null && title != null) tvTitle.setText(title);
            if (tvMessage != null && message != null) tvMessage.setText(message);
            
            // Always show fallback icon (no Lottie animation)
            if (ivEmptyFallback != null) {
                ivEmptyFallback.setVisibility(View.VISIBLE);
            }

            if (btnRetry != null) {
                if (retryAction != null) {
                    btnRetry.setVisibility(View.VISIBLE);
                    btnRetry.setOnClickListener(v -> retryAction.run());
                } else {
                    btnRetry.setVisibility(View.GONE);
                }
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
