package com.nineleaps.BookExplorer.service;

import com.nineleaps.BookExplorer.dto.ReviewInput;
import com.nineleaps.BookExplorer.entity.Review;
import com.nineleaps.BookExplorer.entity.User;
import com.nineleaps.BookExplorer.repository.ReviewRepository;
import com.nineleaps.BookExplorer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public List<Review> getReviewsForBook(String bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    public Float getAverageRating(String bookId) {
        Double avg = reviewRepository.calculateAverageRatingByBookId(bookId);
        return avg != null ? avg.floatValue() : 0.0f;
    }

    public Integer getReviewCount(String bookId) {
        return (int) reviewRepository.countByBookId(bookId);
    }

    @Transactional
    public Review addReview(String email, ReviewInput input) {
        if (input.rating() < 1 || input.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        if (reviewRepository.existsByUserEmailAndBookId(email, input.bookId())) {
            throw new IllegalStateException("You have already reviewed this book.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = new Review();
        review.setUser(user);
        review.setBookId(input.bookId());
        review.setRating(input.rating());
        review.setReview(input.review()); // Using setReview as per your entity

        Review savedReview = reviewRepository.save(review);
        log.debug("Successfully created review ID: {}", savedReview.getId());

        return savedReview;
    }

    @Transactional
    public Review updateReview(String email, Long reviewId, ReviewInput input) {
        if (input.rating() != null && (input.rating() < 1 || input.rating() > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        Review existingReview = reviewRepository.findByIdAndUserEmail(reviewId, email)
                .orElseThrow(() -> new RuntimeException("Review not found or you do not have permission to edit it."));

        if (input.rating() != null) {
            existingReview.setRating(input.rating());
        }
        if (input.review() != null) {
            existingReview.setReview(input.review());
        }

        Review updatedReview = reviewRepository.save(existingReview);
        log.debug("Successfully updated review ID: {}", updatedReview.getId());

        return updatedReview;
    }

    @Transactional
    public Boolean deleteReview(String email, Long reviewId) {
        Review existingReview = reviewRepository.findByIdAndUserEmail(reviewId, email)
                .orElseThrow(() -> new RuntimeException("Review not found or you do not have permission to delete it."));

        reviewRepository.delete(existingReview);
        log.debug("Successfully deleted review ID: {}", reviewId);

        return true;
    }
}