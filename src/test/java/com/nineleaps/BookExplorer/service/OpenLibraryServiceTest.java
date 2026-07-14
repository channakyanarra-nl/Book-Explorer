package com.nineleaps.BookExplorer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nineleaps.BookExplorer.dto.BookDto;
import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenLibraryServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private OpenLibraryWebClient webClientAdapter;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OpenLibraryService openLibraryService;

    private final String TITLE = "dune";
    private final int PAGE = 1;
    private final int LIMIT = 10;
    private final String CACHE_KEY = "search:dune:1:10";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should return cached data if available in Redis")
    void shouldReturnCachedData() {
        // Arrange
        BookSearchResponse cachedResponse = new BookSearchResponse(100, PAGE, LIMIT, Collections.emptyList());

        // Tell Redis mock to return our cached object
        when(valueOperations.get(CACHE_KEY)).thenReturn(cachedResponse);

        // Tell ObjectMapper mock to just return the same object when converting
        when(objectMapper.convertValue(cachedResponse, BookSearchResponse.class)).thenReturn(cachedResponse);

        // Act
        BookSearchResponse result = openLibraryService.searchBooks(TITLE, PAGE, LIMIT);

        // Assert
        assertThat(result.totalResults()).isEqualTo(100);
        verify(webClientAdapter, never()).fetchBooksFromApi(any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should fetch from API and cache data if Redis is empty")
    void shouldFetchFromApiAndCacheWhenRedisEmpty() {
        // Arrange
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
        BookSearchResponse apiResponse = new BookSearchResponse(50, PAGE, LIMIT, Collections.emptyList());
        when(webClientAdapter.fetchBooksFromApi(TITLE, PAGE, LIMIT)).thenReturn(apiResponse);

        // Act
        BookSearchResponse result = openLibraryService.searchBooks(TITLE, PAGE, LIMIT);

        // Assert
        assertThat(result.totalResults()).isEqualTo(50);
        verify(webClientAdapter, times(1)).fetchBooksFromApi(TITLE, PAGE, LIMIT);
        verify(valueOperations, times(1)).set(eq(CACHE_KEY), eq(apiResponse), any(Duration.class));
    }

    @Test
    @DisplayName("Should return cached book details if available in Redis")
    void shouldReturnCachedBookDetails() {
        // Arrange
        String workId = "OL27448W";
        String cacheKey = "book:" + workId;
        BookDto cachedBook = new BookDto(workId, "The Hobbit", null, "A great adventure", null, null, null, null, null, null, null, null);

        when(valueOperations.get(cacheKey)).thenReturn(cachedBook);
        when(objectMapper.convertValue(cachedBook, BookDto.class)).thenReturn(cachedBook);

        // Act
        BookDto result = openLibraryService.getBookDetails(workId);

        // Assert
        assertThat(result.title()).isEqualTo("The Hobbit");
        verify(webClientAdapter, never()).fetchBookDetailsFromApi(anyString());
    }

    @Test
    @DisplayName("Should fetch book from API and cache it when Redis is empty")
    void shouldFetchBookFromApiAndCacheWhenRedisEmpty() {
        // Arrange
        String workId = "OL27448W";
        String cacheKey = "book:" + workId;
        BookDto apiBook = new BookDto(workId, "The Hobbit", null, "A great adventure", null, null, null, null, null, null, null, null);

        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(webClientAdapter.fetchBookDetailsFromApi(workId)).thenReturn(apiBook);

        // Act
        BookDto result = openLibraryService.getBookDetails(workId);

        // Assert
        assertThat(result.title()).isEqualTo("The Hobbit");
        verify(webClientAdapter, times(1)).fetchBookDetailsFromApi(workId);
        verify(valueOperations, times(1)).set(eq(cacheKey), eq(apiBook), any(Duration.class));
    }
}