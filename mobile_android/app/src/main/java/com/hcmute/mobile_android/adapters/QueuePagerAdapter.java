package com.hcmute.mobile_android.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.hcmute.mobile_android.ui.fragments.QueueListFragment;

public class QueuePagerAdapter extends FragmentStateAdapter {

    private QueueListFragment waitingFragment;
    private QueueListFragment subClinicalFragment;
    private QueueListFragment priorityFragment;

    public QueuePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        waitingFragment = QueueListFragment.newInstance();
        subClinicalFragment = QueueListFragment.newInstance();
        priorityFragment = QueueListFragment.newInstance();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return waitingFragment;
            case 1: return subClinicalFragment;
            case 2: return priorityFragment;
            default: return waitingFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public QueueListFragment getWaitingFragment() { return waitingFragment; }
    public QueueListFragment getSubClinicalFragment() { return subClinicalFragment; }
    public QueueListFragment getPriorityFragment() { return priorityFragment; }
}
