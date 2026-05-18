package com.example.shop.model;

import java.util.List;

public class PageResult<T> {
    private final List<T> records;
    private final int page;
    private final int size;
    private final int total;
    private final int totalPages;

    public PageResult(List<T> records, int page, int size, int total) {
        this.records = records;
        this.page = Math.max(page, 1);
        this.size = Math.max(size, 1);
        this.total = Math.max(total, 0);
        this.totalPages = Math.max((int) Math.ceil(this.total * 1.0 / this.size), 1);
    }

    public List<T> getRecords() { return records; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotal() { return total; }
    public int getTotalPages() { return totalPages; }
    public boolean isHasPrevious() { return page > 1; }
    public boolean isHasNext() { return page < totalPages; }
    public int getPreviousPage() { return Math.max(page - 1, 1); }
    public int getNextPage() { return Math.min(page + 1, totalPages); }
    public int getOffset() { return (page - 1) * size; }
}
