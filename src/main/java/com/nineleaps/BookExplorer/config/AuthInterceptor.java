package com.nineleaps.BookExplorer.config;

import com.nineleaps.BookExplorer.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class AuthInterceptor implements WebGraphQlInterceptor {

    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        List<String> authHeaders = request.getHeaders().get("Authorization");

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (header.startsWith("Bearer ")) {
                String token = header.substring(7);

                // Use the clean validation logic
                if (jwtService.isTokenValid(token)) {
                    String email = jwtService.extractUsername(token);

                    // Inject the email into the GraphQL context for our controllers to use
                    request.configureExecutionInput((executionInput, builder) ->
                            builder.graphQLContext(context -> context.put("currentUserEmail", email)).build());
                }
            }
        }

        return chain.next(request);
    }
}