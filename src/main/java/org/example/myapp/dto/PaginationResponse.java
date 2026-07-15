package org.example.myapp.dto;

import java.util.List;

public class PaginationResponse<T> {

    public List<T> items;
    public long totalItems;
    public int page;
    public int size;
    public long totalPages;

    public PaginationResponse(List<T> items, long totalItems, int page, int size) {
        this.items = items;
        this.totalItems = totalItems;
        this.page = page;
        this.size = size;
        this.totalPages = (long) Math.ceil((double) totalItems / size);
    }
}
