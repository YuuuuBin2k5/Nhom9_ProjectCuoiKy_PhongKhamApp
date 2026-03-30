package com.hcmute.mobile_android.network.models;

import java.util.List;

public class AuditLogResponse {
    private List<AuditLog> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public List<AuditLog> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isLast() { return last; }
}
