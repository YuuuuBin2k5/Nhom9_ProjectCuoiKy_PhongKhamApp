package com.hcmute.mobile_android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hcmute.mobile_android.util.ToastUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.MessageResponse;
import com.hcmute.mobile_android.network.models.NotificationItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private ProgressBar progress;
    private TextView tvEmpty, tvMarkAllRead;
    private RecyclerView recycler;
    private NotificationsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress = view.findViewById(R.id.progress);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvMarkAllRead = view.findViewById(R.id.tvMarkAllRead);
        tvMarkAllRead.setOnClickListener(v -> markAllAsRead());
        recycler = view.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationsAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);
        loadNotifications();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            loadNotifications();
        }
    }

    private void markAllAsRead() {
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.markAllNotificationsAsRead().enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    loadNotifications();
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                ToastUtils.showCenteredToast(getContext(), "Lỗi kết nối");
            }
        });
    }

    private void loadNotifications() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getMyNotifications().enqueue(new Callback<List<NotificationItem>>() {
            @Override
            public void onResponse(Call<List<NotificationItem>> call, Response<List<NotificationItem>> response) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<NotificationItem> items = response.body();
                    adapter.setItems(items);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Vui lòng đăng nhập");
                }
            }

            @Override
            public void onFailure(Call<List<NotificationItem>> call, Throwable t) {
                if (!isAdded()) return;
                progress.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(t.getMessage() != null ? t.getMessage() : "Không tải được");
                ToastUtils.showCenteredToast(getContext(), "Lỗi kết nối");
            }
        });
    }

    private static class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
        private List<NotificationItem> items;

        NotificationsAdapter(List<NotificationItem> items) {
            this.items = items;
        }

        void setItems(List<NotificationItem> items) {
            this.items = items != null ? items : new ArrayList<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationItem n = items.get(position);
            holder.tvTitle.setText(n.getTitle() != null ? n.getTitle() : "");
            holder.tvMessage.setText(n.getMessage() != null ? n.getMessage() : "");
            
            // Highlight unread notifications
            if (!n.isRead()) {
                holder.card.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.toothly_pearl));
                holder.tvTitle.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.toothly_teal_dark));
            } else {
                holder.card.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
                holder.tvTitle.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            }

            String time = n.getCreatedAt();
            if (time != null && time.length() > 10) {
                // Formatting "2026-03-24T10:00:00" -> "2026-03-24 10:00:00"
                time = time.replace("T", " ");
                if (time.length() > 19) time = time.substring(0, 19);
            }
            holder.tvTime.setText(time != null ? time : "");

            holder.itemView.setOnClickListener(v -> {
                if (!n.isRead()) {
                    markAsRead(n, holder);
                }
            });
        }

        private void markAsRead(NotificationItem n, ViewHolder holder) {
            ApiService api = RetrofitClient.getApiService(holder.itemView.getContext());
            api.markNotificationAsRead(n.getId()).enqueue(new Callback<MessageResponse>() {
                @Override
                public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                    if (response.isSuccessful()) {
                        n.setRead(true);
                        notifyItemChanged(holder.getAdapterPosition());
                    }
                }

                @Override
                public void onFailure(Call<MessageResponse> call, Throwable t) {}
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMessage, tvTime;
            com.google.android.material.card.MaterialCardView card;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
                card = (com.google.android.material.card.MaterialCardView) itemView;
            }
        }
    }
}
