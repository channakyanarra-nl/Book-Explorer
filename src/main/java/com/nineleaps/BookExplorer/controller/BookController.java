package com.nineleaps.BookExplorer.controller;

import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import com.nineleaps.BookExplorer.service.OpenLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class BookController {

    private final OpenLibraryService openLibraryService;

    BookController(OpenLibraryService openLibraryService){
        this.openLibraryService = openLibraryService;
    }

    @QueryMapping
    public BookSearchResponse searchBooks(@Argument String title, @Argument int page, @Argument int limit){
        return openLibraryService.searchBooks(title, page, limit);
    }


}
