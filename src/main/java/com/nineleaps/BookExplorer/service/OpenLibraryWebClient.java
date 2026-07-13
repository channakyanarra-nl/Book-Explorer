package com.nineleaps.BookExplorer.service;

import com.nineleaps.BookExplorer.dto.AuthorDto;
import com.nineleaps.BookExplorer.dto.BookDto;
import com.nineleaps.BookExplorer.dto.BookSearchResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class OpenLibraryWebClient {

    private final WebClient webClient;

    public OpenLibraryWebClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://openlibrary.org").build();
    }

    public BookSearchResponse fetchBooksFromApi(String title, int page, int limit) {
        int offset = (page - 1) * limit;

        JsonNode response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("q", title)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        System.out.println("API Raw Response: " + (response != null ? response.get("numFound") : "NULL"));

        if (response == null || !response.has("docs")) {
            return new BookSearchResponse(0, page, limit, List.of());
        }

        int totalResults = response.get("numFound").asInt();
        List<BookDto> books = new ArrayList<>();

        for (JsonNode doc : response.get("docs")) {
            books.add(mapNodeToBookDto(doc));
        }

        return new BookSearchResponse(totalResults, page, limit, books);
    }

    private BookDto mapNodeToBookDto(JsonNode doc) {
        // To Get ID by removing the "/works/" in the open library response
        String rawKey = doc.has("key") ? doc.get("key").asText() : "";
        String id = rawKey.replace("/works/", "");

        String title = doc.has("title") ? doc.get("title").asText() : null;
        Integer publishYear = doc.has("first_publish_year") ? doc.get("first_publish_year").asInt() : null;

        // Extract first ISBN if available
        String isbn = (doc.has("isbn") && doc.get("isbn").isArray() && !doc.get("isbn").isEmpty())
                ? doc.get("isbn").get(0).asText() : null;

        // OpenLibrary cover image API uses cover_i
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

        // Return the DTO (metrics and favourite status are null for now)
        return new BookDto(id, title, null, null, publishYear, isbn, coverImage, authors, null, null, null, null);
    }
}
