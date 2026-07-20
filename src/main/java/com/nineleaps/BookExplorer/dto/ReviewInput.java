package com.nineleaps.BookExplorer.dto;

public record ReviewInput(
        String bookId,
        Integer rating,
        String review
) {}