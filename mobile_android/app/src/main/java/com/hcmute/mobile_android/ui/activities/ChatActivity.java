package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hcmute.mobile_android.BuildConfig;
import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.network.ApiService;
import com.hcmute.mobile_android.network.RetrofitClient;
import com.hcmute.mobile_android.network.models.ChatMessagePayload;
import com.hcmute.mobile_android.network.models.ChatSendBody;
import com.hcmute.mobile_android.util.TokenManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private ChatAdapter adapter;
    private final List<ChatRow> rows = new ArrayList<>();
    private final Set<Long> seenIds = new HashSet<>();
    private EditText etMessage;
    private String doctorName;
    private long doctorId = -1;
    private ApiService api;
    private final Gson gson = new Gson();
    private WebSocket webSocket;
    private OkHttpClient wsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        doctorName = getIntent().getStringExtra("doctorName");
        doctorId = getIntent().getLongExtra("doctorId", -1L);

        if (doctorId <= 0) {
            Toast.makeText(this, "Thiếu bác sĩ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TokenManager tm = new TokenManager(this);
        if (tm.getToken() == null || tm.getToken().isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        api = RetrofitClient.getApiService(this);
        initViews();
        loadHistory();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvToolbarName = findViewById(R.id.tvToolbarDoctorName);
        if (doctorName != null) tvToolbarName.setText(doctorName);

        rvMessages = findViewById(R.id.rvChatMessages);
        etMessage = findViewById(R.id.etChatMessage);
        View btnSend = findViewById(R.id.btnSendMessage);

        adapter = new ChatAdapter(rows);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadHistory() {
        api.getChatMessages(doctorId).enqueue(new Callback<List<ChatMessagePayload>>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatMessagePayload>> call, @NonNull Response<List<ChatMessagePayload>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rows.clear();
                    seenIds.clear();
                    for (ChatMessagePayload p : response.body()) {
                        addPayloadQuiet(p);
                    }
                    adapter.notifyDataSetChanged();
                    scrollLast();
                } else {
                    Toast.makeText(ChatActivity.this, "Không tải được tin nhắn", Toast.LENGTH_SHORT).show();
                }
                connectWebSocket();
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatMessagePayload>> call, @NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi tải tin nhắn", Toast.LENGTH_SHORT).show();
                connectWebSocket();
            }
        });
    }

    private void addPayloadQuiet(ChatMessagePayload p) {
        if (p.getId() != null) {
            if (seenIds.contains(p.getId())) return;
            seenIds.add(p.getId());
        }
        rows.add(ChatRow.fromPayload(p));
    }

    private void connectWebSocket() {
        String url = buildChatWsUrl();
        if (url == null) {
            return;
        }
        wsClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        webSocket = wsClient.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                runOnUiThread(() -> handleWsJson(text));
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, okhttp3.Response response) {
                // Im lặng — có thể mất WS nhưng vẫn chat qua REST
            }
        });
    }

    private void handleWsJson(String text) {
        try {
            JsonObject o = gson.fromJson(text, JsonObject.class);
            if (!o.has("type") || !"CHAT_MESSAGE".equals(o.get("type").getAsString())) {
                return;
            }
            if (!o.has("payload")) return;
            ChatMessagePayload p = gson.fromJson(o.get("payload"), ChatMessagePayload.class);
            if (p.getId() != null && seenIds.contains(p.getId())) {
                return;
            }
            addPayloadQuiet(p);
            adapter.notifyItemInserted(rows.size() - 1);
            scrollLast();
        } catch (Exception ignored) {
        }
    }

    private String buildChatWsUrl() {
        TokenManager tm = new TokenManager(this);
        String token = tm.getToken();
        if (token == null || token.isEmpty()) return null;
        String base = BuildConfig.API_BASE_URL;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String ws = base.replace("http://", "ws://").replace("https://", "wss://");
        try {
            return ws + "/ws/chat?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8.name())
                    + "&doctorId=" + doctorId;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        api.sendChatMessage(doctorId, new ChatSendBody(text)).enqueue(new Callback<ChatMessagePayload>() {
            @Override
            public void onResponse(@NonNull Call<ChatMessagePayload> call, @NonNull Response<ChatMessagePayload> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatMessagePayload p = response.body();
                    if (p.getId() != null && seenIds.contains(p.getId())) {
                        etMessage.setText("");
                        return;
                    }
                    addPayloadQuiet(p);
                    adapter.notifyItemInserted(rows.size() - 1);
                    etMessage.setText("");
                    scrollLast();
                } else {
                    Toast.makeText(ChatActivity.this, "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatMessagePayload> call, @NonNull Throwable t) {
                Toast.makeText(ChatActivity.this, "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scrollLast() {
        if (!rows.isEmpty()) {
            rvMessages.scrollToPosition(rows.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, null);
            webSocket = null;
        }
    }

    private static class ChatRow {
        final String text;
        final boolean mine;

        ChatRow(String text, boolean mine) {
            this.text = text;
            this.mine = mine;
        }

        static ChatRow fromPayload(ChatMessagePayload p) {
            return new ChatRow(
                    p.getContent() != null ? p.getContent() : "",
                    p.isFromPatient()
            );
        }
    }

    private static class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<ChatRow> messages;

        ChatAdapter(List<ChatRow> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).mine ? 1 : 0;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_right, parent, false);
                return new Holder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_left, parent, false);
                return new Holder(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((Holder) holder).tvText.setText(messages.get(position).text);
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView tvText;

            Holder(View v) {
                super(v);
                tvText = v.findViewById(R.id.tvMessage);
            }
        }
    }
}
