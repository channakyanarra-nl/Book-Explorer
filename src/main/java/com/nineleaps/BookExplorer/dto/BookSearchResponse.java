package com.nineleaps.BookExplorer.dto;

import java.util.List;

public record BookSearchResponse(
        Integer totalResults,
        Integer page,
        Integer limit,
        List<BookDto> books
) {}