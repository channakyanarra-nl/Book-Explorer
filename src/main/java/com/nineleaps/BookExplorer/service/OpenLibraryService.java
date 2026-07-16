package com.nineleaps.BookExplorer.service;

import com.nineleaps.BookExplorer.dto.BookDto;
import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j; // 1. Import Lombok logging
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class OpenLibraryService {

    private final OpenLibraryWebClient webClientAdapter;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public OpenLibraryService(OpenLibraryWebClient webClientAdapter,
                              RedisTemplate<String, Object> redisTemplate,
                              ObjectMapper objectMapper) {
        this.webClientAdapter = webClientAdapter;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public BookSearchResponse searchBooks(String title, int page, int limit) {
        String cacheKey = String.format("search:%s:%d:%d", title, page, limit);
        log.debug("Searching for books with title: '{}' (Page: {}, Limit: {})", title, page, limit);

        // 1. Check Cache
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Redis Cache HIT for search key: {}", cacheKey);
            return objectMapper.convertValue(cached, BookSearchResponse.class);
        }

        // 2. Fetch from API
        log.info("Redis Cache MISS for search key: {}. Fetching from OpenLibrary API...", cacheKey);
        BookSearchResponse response = webClientAdapter.fetchBooksFromApi(title, page, limit);

        // 3. Save to Cache (cache for 1 hour to keep it fresh)
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(1));
        log.debug("Saved search results to Redis cache: {}", cacheKey);

        return response;
    }

    public BookDto getBookDetails(String workId) {
        String cacheKey = "book:" + workId;
        log.debug("Fetching book details for workId: {}", workId);

        // 1. Check Redis Cache
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Redis Cache HIT for book details: {}", cacheKey);
            return objectMapper.convertValue(cached, BookDto.class);
        }

        // 2. Cache miss -> Fetch from API
        log.info("Redis Cache MISS for book details: {}. Fetching from OpenLibrary API...", cacheKey);
        BookDto book = webClientAdapter.fetchBookDetailsFromApi(workId);

        // 3. Save to Redis (Cache for 1 day)
        if (book != null) {
            redisTemplate.opsForValue().set(cacheKey, book, Duration.ofDays(1));
            log.debug("Saved book details to Redis cache: {}", cacheKey);
        } else {
            log.warn("No book details found in OpenLibrary API for workId: {}", workId);
        }

        return book;
    }
}