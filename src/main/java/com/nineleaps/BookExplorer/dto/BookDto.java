package com.nineleaps.BookExplorer.dto;

import java.util.List;

public record BookDto(
        String id,
        String title,
        String subtitle,
        String description,
        Integer publishYear,
        String isbn,
        String coverImage,
        List<AuthorDto> authors,
        List<String> subjects,
        Float averageRating,
        Integer reviewCount,
        Boolean isFavourite
) {}
