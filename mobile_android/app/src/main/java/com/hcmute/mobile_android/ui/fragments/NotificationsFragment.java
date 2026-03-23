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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.NotificationItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private ProgressBar progress;
    private TextView tvEmpty;
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

    private void loadNotifications() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        ApiService api = RetrofitClient.getApiService(requireContext());
        api.getMyNotifications().enqueue(new Callback<List<NotificationItem>>() {
            @Override
            public void onResponse(Call<List<NotificationItem>> call, Response<List<NotificationItem>> response) {
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
                progress.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText(t.getMessage() != null ? t.getMessage() : "Không tải được");
                Toast.makeText(requireContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
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
            String time = n.getCreatedAt();
            if (time != null && time.length() > 10) {
                time = time.substring(0, 10) + " " + (time.length() > 19 ? time.substring(11, 19) : "");
            }
            holder.tvTime.setText(time != null ? time : "");
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMessage, tvTime;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvMessage = itemView.findViewById(R.id.tvMessage);
                tvTime = itemView.findViewById(R.id.tvTime);
            }
        }
    }
}
