package com.nineleaps.BookExplorer.controller;

import com.nineleaps.BookExplorer.dto.BookDto;
import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import com.nineleaps.BookExplorer.entity.Review;
import com.nineleaps.BookExplorer.repository.ReviewRepository;
import com.nineleaps.BookExplorer.service.OpenLibraryService;
import lombok.extern.slf4j.Slf4j; // 1. Import Lombok logging
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
public class BookController {

    private final OpenLibraryService openLibraryService;
    private final ReviewRepository reviewRepository;

    BookController(OpenLibraryService openLibraryService, ReviewRepository reviewRepository){
        this.openLibraryService = openLibraryService;
        this.reviewRepository = reviewRepository;
    }

    @QueryMapping
    public BookSearchResponse searchBooks(@Argument String title, @Argument int page, @Argument int limit){
        log.info("GraphQL Query: searchBooks -> title: '{}', page: {}, limit: {}", title, page, limit);
        return openLibraryService.searchBooks(title, page, limit);
    }

    @QueryMapping
    public BookDto book(@Argument String id) {
        log.info("GraphQL Query: book -> id: '{}'", id);
        return openLibraryService.getBookDetails(id);
    }

    @SchemaMapping(typeName = "Book", field = "reviews")
    public List<Review> reviews(BookDto book) {
        log.debug("GraphQL Resolver: Fetching reviews for book id: '{}'", book.id());
        return reviewRepository.findByBookId(book.id());
    }

    @SchemaMapping(typeName = "Book", field = "averageRating")
    public Float averageRating(BookDto book) {
        log.debug("GraphQL Resolver: Calculating average rating for book id: '{}'", book.id());
        Double avg = reviewRepository.calculateAverageRatingByBookId(book.id());
        return avg != null ? avg.floatValue() : 0.0f;
    }

    @SchemaMapping(typeName = "Book", field = "reviewCount")
    public Integer reviewCount(BookDto book) {
        log.debug("GraphQL Resolver: Counting reviews for book id: '{}'", book.id());
        return (int) reviewRepository.countByBookId(book.id());
    }

    @SchemaMapping(typeName = "Book", field = "isFavourite")
    public Boolean isFavourite(BookDto book) {
        log.debug("GraphQL Resolver: Checking favourite status for book id: '{}'", book.id());
        // We will wire this up to the actual user context soon!
        return false;
    }
}