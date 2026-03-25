package com.hcmute.mobile_android.ui.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.hcmute.mobile_android.R;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private EditText etMessage;
    private String doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        doctorName = getIntent().getStringExtra("doctorName");

        initViews();
        setupMockMessages();
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

        adapter = new ChatAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupMockMessages() {
        messageList.add(new ChatMessage("Xin chào bác sĩ!", true));
        messageList.add(new ChatMessage("Chào bạn, tôi có thể giúp gì cho bạn?", false));
        adapter.notifyDataSetChanged();
        rvMessages.scrollToPosition(messageList.size() - 1);
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            messageList.add(new ChatMessage(text, true));
            etMessage.setText("");
            adapter.notifyItemInserted(messageList.size() - 1);
            rvMessages.scrollToPosition(messageList.size() - 1);
            
            // Auto reply mock
            rvMessages.postDelayed(() -> {
                messageList.add(new ChatMessage("Bác sĩ đã nhận được tin nhắn và sẽ phản hồi sớm.", false));
                adapter.notifyItemInserted(messageList.size() - 1);
                rvMessages.scrollToPosition(messageList.size() - 1);
            }, 1000);
        }
    }

    private static class ChatMessage {
        String text;
        boolean isMine;

        ChatMessage(String text, boolean isMine) {
            this.text = text;
            this.isMine = isMine;
        }
    }

    private static class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<ChatMessage> messages;

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isMine ? 1 : 0;
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
            TextView tvText;
            Holder(View v) {
                super(v);
                tvText = v.findViewById(R.id.tvMessage);
            }
        }
    }
}
