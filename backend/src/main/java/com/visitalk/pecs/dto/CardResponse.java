package com.visitalk.pecs.dto;

public record CardResponse(
    Long id,
    String category,
    String imageUrl,
    String labelI18n,
    boolean isCustom,
    int sortOrder
) {}
