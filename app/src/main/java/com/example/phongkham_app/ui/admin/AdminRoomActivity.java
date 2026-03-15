package com.example.phongkham_app.ui.admin;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phongkham_app.R;
import com.example.phongkham_app.data.local.DatabaseHelper;

public class AdminRoomActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvRooms;
    private RoomAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_room);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadRooms();
    }

    private void initViews() {
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        rvRooms = findViewById(R.id.rvRooms);
        rvRooms.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void loadRooms() {
        Cursor cursor = dbHelper.getRoomsWithWaitingCount();
        if (adapter == null) {
            adapter = new RoomAdminAdapter(cursor);
            rvRooms.setAdapter(adapter);
        } else {
            adapter.swapCursor(cursor);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRooms();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.swapCursor(null);
        }
    }
}
