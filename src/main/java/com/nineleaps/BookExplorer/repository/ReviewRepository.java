package com.nineleaps.BookExplorer.repository;

import com.nineleaps.BookExplorer.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookId(String bookId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.bookId = :bookId")
    Double calculateAverageRatingByBookId(@Param("bookId") String bookId);

    boolean existsByUserIdAndBookId(Long userId, String bookId);

    boolean existsByUserEmailAndBookId(String email, String bookId);

    Optional<Review> findByIdAndUserEmail(Long id, String email);

    long countByBookId(String bookId);
}
