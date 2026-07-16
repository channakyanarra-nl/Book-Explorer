package com.nineleaps.BookExplorer.service;

import com.nineleaps.BookExplorer.dto.AuthorDto;
import com.nineleaps.BookExplorer.dto.BookDto;
import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j; // 1. Added Lombok logging
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OpenLibraryWebClient {

    private final RestClient restClient;

    public OpenLibraryWebClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://openlibrary.org").build();
    }

    public BookSearchResponse fetchBooksFromApi(String title, int page, int limit) {
        int offset = (page - 1) * limit;
        log.info("Sending HTTP GET to OpenLibrary API for search query: '{}' (offset: {}, limit: {})", title, offset, limit);

        JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("q", title)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        // 3. Replaced System.out.println with log.debug
        log.debug("OpenLibrary API Search Raw Response numFound: {}",
                (response != null && response.has("numFound")) ? response.get("numFound").asText() : "NULL");

        if (response == null || !response.has("docs")) {
            log.warn("OpenLibrary API returned null or missing 'docs' for search query: '{}'", title);
            return new BookSearchResponse(0, page, limit, List.of());
        }

        int totalResults = response.get("numFound").asInt();
        List<BookDto> books = new ArrayList<>();

        for (JsonNode doc : response.get("docs")) {
            books.add(mapNodeToBookDto(doc));
        }

        log.info("Successfully parsed {} books from OpenLibrary API response", books.size());
        return new BookSearchResponse(totalResults, page, limit, books);
    }

    public BookDto fetchBookDetailsFromApi(String workId) {
        log.info("Sending HTTP GET to OpenLibrary API for book details: {}", workId);

        JsonNode response = restClient.get()
                .uri("/works/{workId}.json", workId)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            log.warn("OpenLibrary API returned null response for workId: {}", workId);
            return null;
        }

        String title = response.has("title") ? response.get("title").asText() : null;

        // Description parsing
        String description = null;
        if (response.has("description")) {
            JsonNode descNode = response.get("description");
            description = descNode.isObject() && descNode.has("value")
                    ? descNode.get("value").asText()
                    : descNode.asText();
        }

        // Extract the first cover ID if available
        String coverImage = null;
        if (response.has("covers") && response.get("covers").isArray() && !response.get("covers").isEmpty()) {
            coverImage = "https://covers.openlibrary.org/b/id/" + response.get("covers").get(0).asText() + "-L.jpg";
        }

        // Extract subjects
        List<String> subjects = new ArrayList<>();
        if (response.has("subjects")) {
            for (JsonNode subject : response.get("subjects")) {
                subjects.add(subject.asText());
            }
        }

        log.debug("Successfully parsed book details for workId: {}", workId);
        return new BookDto(workId, title, null, description, null, null, coverImage, null, subjects, null, null, null);
    }

    private BookDto mapNodeToBookDto(JsonNode doc) {
        String rawKey = doc.has("key") ? doc.get("key").asText() : "";
        String id = rawKey.replace("/works/", "");

        String title = doc.has("title") ? doc.get("title").asText() : null;
        Integer publishYear = doc.has("first_publish_year") ? doc.get("first_publish_year").asInt() : null;

        String isbn = (doc.has("isbn") && doc.get("isbn").isArray() && !doc.get("isbn").isEmpty())
                ? doc.get("isbn").get(0).asText() : null;

        String coverImage = doc.has("cover_i")
                ? "https://covers.openlibrary.org/b/id/" + doc.get("cover_i").asText() + "-L.jpg" : null;

        List<AuthorDto> authors = new ArrayList<>();
        if (doc.has("author_name") && doc.has("author_key")) {
            for (int i = 0; i < doc.get("author_name").size(); i++) {
                String authorName = doc.get("author_name").get(i).asText();
                String authorId = doc.get("author_key").size() > i ? doc.get("author_key").get(i).asText() : null;
                authors.add(new AuthorDto(authorId, authorName));
            }
        }

        return new BookDto(id, title, null, null, publishYear, isbn, coverImage, authors, null, null, null, null);
    }
}