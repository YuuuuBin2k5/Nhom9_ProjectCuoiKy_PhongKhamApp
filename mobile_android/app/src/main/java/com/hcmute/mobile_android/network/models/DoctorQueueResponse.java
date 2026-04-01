package com.hcmute.mobile_android.network.models;

import java.util.List;

public class DoctorQueueResponse {
    private List<QueueItem> queuedPatients;
    private List<QueueItem> transferredPatients;

    public List<QueueItem> getQueuedPatients() {
        return queuedPatients;
    }

    public void setQueuedPatients(List<QueueItem> queuedPatients) {
        this.queuedPatients = queuedPatients;
    }

    public List<QueueItem> getTransferredPatients() {
        return transferredPatients;
    }

    public void setTransferredPatients(List<QueueItem> transferredPatients) {
        this.transferredPatients = transferredPatients;
    }
}
