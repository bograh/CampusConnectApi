package com.campusconnect.api.config;

import java.util.List;

public record CachedPage<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        String sort,
        long totalElements,
        long totalPages,
        boolean first,
        boolean last
) {}
