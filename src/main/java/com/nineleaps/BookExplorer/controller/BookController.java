package com.nineleaps.BookExplorer.controller;

import com.nineleaps.BookExplorer.dto.BookDto;
import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import com.nineleaps.BookExplorer.dto.FavouriteResponse;
import com.nineleaps.BookExplorer.dto.ReviewInput;
import com.nineleaps.BookExplorer.entity.Review;
import com.nineleaps.BookExplorer.service.FavouriteService;
import com.nineleaps.BookExplorer.service.OpenLibraryService;
import com.nineleaps.BookExplorer.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
public class BookController {

    private final OpenLibraryService openLibraryService;
    private final ReviewService reviewService;
    private final FavouriteService favouriteService;

    BookController(
            OpenLibraryService openLibraryService,
            ReviewService reviewService,
            FavouriteService favouriteService){
        this.openLibraryService = openLibraryService;
        this.reviewService = reviewService;
        this.favouriteService = favouriteService;
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
        return reviewService.getReviewsForBook(book.id());
    }

    @SchemaMapping(typeName = "Book", field = "averageRating")
    public Float averageRating(BookDto book) {
        log.debug("GraphQL Resolver: Calculating average rating for book id: '{}'", book.id());
        return reviewService.getAverageRating(book.id());
    }

    @SchemaMapping(typeName = "Book", field = "reviewCount")
    public Integer reviewCount(BookDto book) {
        log.debug("GraphQL Resolver: Counting reviews for book id: '{}'", book.id());
        return reviewService.getReviewCount(book.id());
    }

    @SchemaMapping(typeName = "Book", field = "isFavourite")
    public Boolean isFavourite(BookDto book, @ContextValue(name = "currentUserEmail", required = false) String email) {
        if (email == null) return false;

        log.debug("GraphQL Resolver: Checking favourite status for book id: '{}' against user: '{}'", book.id(), email);
        return favouriteService.isFavourite(email, book.id());
    }

    @MutationMapping
    public FavouriteResponse addFavourite(@Argument String bookId, @ContextValue(name = "currentUserEmail", required = false) String email) {
        if (email == null) {
            log.warn("Unauthorized attempt to add favourite for book: '{}'", bookId);
            return new FavouriteResponse(false, "Unauthorized: Please log in to add favourites.");
        }

        log.info("GraphQL Mutation: addFavourite -> User: '{}', Book: '{}'", email, bookId);
        return favouriteService.addFavourite(email, bookId);
    }

    @MutationMapping
    public FavouriteResponse removeFavourite(@Argument String bookId, @ContextValue(name = "currentUserEmail", required = false) String email) {
        if (email == null) {
            log.warn("Unauthorized attempt to remove favourite for book: '{}'", bookId);
            return new FavouriteResponse(false, "Unauthorized: Please log in to remove favourites.");
        }

        log.info("GraphQL Mutation: removeFavourite -> User: '{}', Book: '{}'", email, bookId);
        return favouriteService.removeFavourite(email, bookId);
    }

    @MutationMapping
    public Review addReview(
            @Argument ReviewInput input,
            @ContextValue(name = "currentUserEmail", required = false) String email) {

        if (email == null) {
            log.warn("Unauthorized addReview attempt for book: '{}'", input.bookId());
            throw new RuntimeException("Unauthorized: Please log in to leave a review.");
        }

        log.info("GraphQL Mutation: addReview -> User: '{}', Book: '{}'", email, input.bookId());
        return reviewService.addReview(email, input);
    }

    @MutationMapping
    public Review updateReview(
            @Argument Long reviewId,
            @Argument ReviewInput input,
            @ContextValue(name = "currentUserEmail", required = false) String email) {

        if (email == null) {
            log.warn("Unauthorized updateReview attempt for review ID: {}", reviewId);
            throw new RuntimeException("Unauthorized: Please log in to update a review.");
        }

        log.info("GraphQL Mutation: updateReview -> User: '{}', Review ID: {}", email, reviewId);
        return reviewService.updateReview(email, reviewId, input);
    }

    @MutationMapping
    public Boolean deleteReview(
            @Argument Long reviewId,
            @ContextValue(name = "currentUserEmail", required = false) String email) {

        if (email == null) {
            log.warn("Unauthorized deleteReview attempt for review ID: {}", reviewId);
            throw new RuntimeException("Unauthorized: Please log in to delete a review.");
        }

        log.info("GraphQL Mutation: deleteReview -> User: '{}', Review ID: {}", email, reviewId);
        return reviewService.deleteReview(email, reviewId);
    }
}