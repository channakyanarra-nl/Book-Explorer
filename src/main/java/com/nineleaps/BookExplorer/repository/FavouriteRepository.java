package com.nineleaps.BookExplorer.repository;

import com.nineleaps.BookExplorer.entity.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {

    // Checks if a specific user has favorited a specific book
    boolean existsByUserEmailAndBookId(String email, String bookId);

    // Deletes the favorite mapping
    @Transactional
    void deleteByUserEmailAndBookId(String email, String bookId);
}