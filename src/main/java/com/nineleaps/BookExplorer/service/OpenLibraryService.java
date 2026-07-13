package com.nineleaps.BookExplorer.service;

import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class OpenLibraryService {

    private final OpenLibraryWebClient webClientAdapter;
  //  private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public OpenLibraryService(OpenLibraryWebClient webClientAdapter,
                              ObjectMapper objectMapper) {
        this.webClientAdapter = webClientAdapter;
     //   this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public BookSearchResponse searchBooks(String title, int page, int limit) {
//        String cacheKey = String.format("search:%s:%d:%d", title, page, limit);
//
//        // 1. Check Cache
//        Object cached = redisTemplate.opsForValue().get(cacheKey);
//        if (cached != null) {
//            // RedisTemplate might return a LinkedHashMap, so we ensure it maps to our record
//            return objectMapper.convertValue(cached, BookSearchResponse.class);
//        }

        // 2. Fetch from API
        BookSearchResponse response = webClientAdapter.fetchBooksFromApi(title, page, limit);

        // 3. Save to Cache (cache for 1 hour to keep it fresh)
     //   redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(1));

        return response;
    }
}
