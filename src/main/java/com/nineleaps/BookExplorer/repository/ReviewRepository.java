package com.nineleaps.BookExplorer.repository;

import com.nineleaps.BookExplorer.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookId(String bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookId = :bookId")
    Double calculateAverageRatingByBookId(@Param("bookId") String bookId);

    boolean existsByUserIdAndBookId(Long userId, String bookId);
}
