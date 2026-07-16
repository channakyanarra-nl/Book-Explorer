package com.nineleaps.BookExplorer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class RequestLoggingInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        long startTime = System.currentTimeMillis();

        // 1. Identify the operation (e.g., "LoginMutation" or "GetBooksQuery")
        String operationName = request.getOperationName() != null
                ? request.getOperationName()
                : "Anonymous Operation";

        log.info("GraphQL Request Started: Operation [{}]", operationName);

        // Use DEBUG level so we don't spam the console in production,
        // but we can see the exact query and variables during development.
        log.debug("GraphQL Document: {}", request.getDocument());
        log.debug("GraphQL Variables: {}", request.getVariables());

        // 2. Let the request execute, then log the result and execution time
        return chain.next(request).doOnNext(response -> {
            long duration = System.currentTimeMillis() - startTime;

            if (response.getErrors().isEmpty()) {
                log.info("GraphQL Request Completed: Operation [{}] in {}ms", operationName, duration);
            } else {
                log.warn("GraphQL Request Completed with Errors: Operation [{}] in {}ms. Error count: {}",
                        operationName, duration, response.getErrors().size());
            }
        });
    }
}