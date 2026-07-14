package com.nineleaps.BookExplorer.controller;

import com.nineleaps.BookExplorer.dto.BookDto;
import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import com.nineleaps.BookExplorer.entity.Review;
import com.nineleaps.BookExplorer.repository.ReviewRepository;
import com.nineleaps.BookExplorer.service.OpenLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

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
        return openLibraryService.searchBooks(title, page, limit);
    }

    @QueryMapping
    public BookDto book(@Argument String id) {
        return openLibraryService.getBookDetails(id);
    }

    @SchemaMapping(typeName = "Book", field = "reviews")
    public List<Review> reviews(BookDto book) {
        return reviewRepository.findByBookId(book.id());
    }

    @SchemaMapping(typeName = "Book", field = "averageRating")
    public Float averageRating(BookDto book) {
        Double avg = reviewRepository.calculateAverageRatingByBookId(book.id());
        return avg != null ? avg.floatValue() : 0.0f;
    }

    @SchemaMapping(typeName = "Book", field = "reviewCount")
    public Integer reviewCount(BookDto book) {
        return (int) reviewRepository.countByBookId(book.id());
    }

    @SchemaMapping(typeName = "Book", field = "isFavourite")
    public Boolean isFavourite(BookDto book) {
        return false;
    }


}
