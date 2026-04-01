package com.hcmute.mobile_android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hcmute.mobile_android.R;
import com.hcmute.mobile_android.adapters.QueueAdapter;
import com.hcmute.mobile_android.network.models.QueueItem;

import java.util.ArrayList;
import java.util.List;

public class QueueListFragment extends Fragment {

    private RecyclerView rvQueue;
    private SwipeRefreshLayout swipeRefresh;
    private QueueAdapter queueAdapter;
    private List<QueueItem> queueList = new ArrayList<>();
    private QueueAdapter.OnQueueActionListener listener;

    public interface OnRefreshRequestedListener {
        void onRefreshRequested();
    }
    private OnRefreshRequestedListener refreshListener;

    public static QueueListFragment newInstance() {
        return new QueueListFragment();
    }

    /** Call this immediately after creating the fragment to set the action listener. */
    public void setListener(QueueAdapter.OnQueueActionListener l) {
        this.listener = l;
        if (queueAdapter != null) {
            // Re-create adapter if already initialized so it gets the listener
            queueAdapter = new QueueAdapter(queueList, listener);
            if (rvQueue != null) rvQueue.setAdapter(queueAdapter);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof QueueAdapter.OnQueueActionListener) {
            listener = (QueueAdapter.OnQueueActionListener) context;
        }
        if (context instanceof OnRefreshRequestedListener) {
            refreshListener = (OnRefreshRequestedListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_queue_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvQueue = view.findViewById(R.id.rvQueue);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        rvQueue.setLayoutManager(new LinearLayoutManager(getContext()));
        queueAdapter = new QueueAdapter(queueList, listener);
        rvQueue.setAdapter(queueAdapter);

        swipeRefresh.setOnRefreshListener(() -> {
            if (refreshListener != null) {
                refreshListener.onRefreshRequested();
            } else {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    public void updateList(List<QueueItem> newList) {
        queueList.clear();
        queueList.addAll(newList);
        if (queueAdapter != null) {
            queueAdapter.notifyDataSetChanged();
        }
        if (swipeRefresh != null) {
            swipeRefresh.setRefreshing(false);
        }
    }
}
