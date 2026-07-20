package com.nineleaps.BookExplorer.service;

import com.nineleaps.BookExplorer.dto.FavouriteResponse;
import com.nineleaps.BookExplorer.entity.Favourite;
import com.nineleaps.BookExplorer.entity.User;
import com.nineleaps.BookExplorer.repository.FavouriteRepository;
import com.nineleaps.BookExplorer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class FavouriteService {

    private final FavouriteRepository favouriteRepository;
    private final UserRepository userRepository;

    public FavouriteService(FavouriteRepository favouriteRepository, UserRepository userRepository) {
        this.favouriteRepository = favouriteRepository;
        this.userRepository = userRepository;
    }

    public Boolean isFavourite(String email, String bookId) {
        return favouriteRepository.existsByUserEmailAndBookId(email, bookId);
    }

    @Transactional
    public FavouriteResponse addFavourite(String email, String bookId) {
        if (favouriteRepository.existsByUserEmailAndBookId(email, bookId)) {
            log.warn("Book '{}' is already a favourite for user '{}'", bookId, email);
            return new FavouriteResponse(false, "This book is already in your favourites.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found in database"));

        Favourite favourite = new Favourite();
        favourite.setUser(user);
        favourite.setBookId(bookId);

        favouriteRepository.save(favourite);
        log.debug("Successfully saved favourite to database");

        return new FavouriteResponse(true, "Book successfully added to favourites.");
    }

    @Transactional
    public FavouriteResponse removeFavourite(String email, String bookId) {
        if (!favouriteRepository.existsByUserEmailAndBookId(email, bookId)) {
            log.warn("Book '{}' is not a favourite for user '{}', cannot remove", bookId, email);
            return new FavouriteResponse(false, "This book is not in your favourites.");
        }

        favouriteRepository.deleteByUserEmailAndBookId(email, bookId);
        log.debug("Successfully removed favourite from database");

        return new FavouriteResponse(true, "Book successfully removed from favourites.");
    }
}