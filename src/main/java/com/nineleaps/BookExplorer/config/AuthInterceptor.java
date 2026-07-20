package com.nineleaps.BookExplorer.config;

import com.nineleaps.BookExplorer.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Configuration
public class AuthInterceptor implements WebGraphQlInterceptor {

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        List<String> authHeaders = request.getHeaders().get("Authorization");

        log.debug("AuthInterceptor triggered. Available headers: {}", request.getHeaders().keySet());

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);

            if (header.startsWith("Bearer ")) {
                String token = header.substring(7);
                log.debug("Bearer token extracted. Validating...");

                try {
                    // Use the clean validation logic
                    if (jwtService.isTokenValid(token)) {
                        String email = jwtService.extractUsername(token);
                        log.info("JWT valid! Injecting user '{}' into GraphQL context.", email);

                        // Inject the email into the GraphQL context for our controllers to use
                        request.configureExecutionInput((executionInput, builder) ->
                                builder.graphQLContext(context -> context.put("currentUserEmail", email)).build());
                    } else {
                        log.warn("JWT validation failed: jwtService.isTokenValid() returned false.");
                    }
                } catch (Exception e) {
                    log.error("Exception occurred while parsing or validating JWT: {}", e.getMessage());
                }
            } else {
                log.warn("Authorization header found, but it does not start with 'Bearer '");
            }
        } else {
            log.debug("No Authorization header found in the GraphQL request.");
        }

        return chain.next(request);
    }
}