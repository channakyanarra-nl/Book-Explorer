package com.nineleaps.BookExplorer.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j; // 1. Add Lombok logging
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j // 2. Enable the logger
@ControllerAdvice
public class GlobalExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        // 1. Handle Duplicate Emails (BAD_REQUEST)
        if (ex instanceof EmailAlreadyInUseException) {
            // Use WARN for user errors (the system isn't broken, the user just made a mistake)
            log.warn("GraphQL Client Error - Duplicate Email: {}", ex.getMessage());
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .build();
        }

        // 2. Handle Resource Not Found (NOT_FOUND)
        if (ex instanceof ResourceNotFoundException) {
            log.warn("GraphQL Client Error - Resource Not Found: {}", ex.getMessage());
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.NOT_FOUND)
                    .message(ex.getMessage())
                    .build();
        }

        // 3. Handle @Valid validation failures for Controller/Schema parameters
        if (ex instanceof HandlerMethodValidationException validationEx) {
            Map<String, String> errors = new HashMap<>();
            validationEx.getParameterValidationResults().forEach(result -> {
                String argumentName = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error -> {
                    errors.put(argumentName, error.getDefaultMessage());
                });
            });

            log.warn("GraphQL Client Error - Validation Failed: {}", errors);
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message("Validation Failed")
                    .extensions(Map.of("details", errors))
                    .build();
        }

        // 4. Alternative validation handler for constraints on arguments
        if (ex instanceof ConstraintViolationException constraintEx) {
            Map<String, String> errors = new HashMap<>();
            constraintEx.getConstraintViolations().forEach(violation -> {
                errors.put(violation.getPropertyPath().toString(), violation.getMessage());
            });

            log.warn("GraphQL Client Error - Constraint Violation: {}", errors);
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message("Validation Failed")
                    .extensions(Map.of("details", errors))
                    .build();
        }

        // 5. All other unexpected exceptions (INTERNAL_ERROR)
        // Use ERROR here and pass the full 'ex' object so the stack trace prints in your logs!
        log.error("GraphQL System Error - Unexpected Exception during execution", ex);
        return GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Unexpected error occurred")
                .build();
    }
}