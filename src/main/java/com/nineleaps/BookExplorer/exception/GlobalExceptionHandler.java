package com.nineleaps.BookExplorer.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {

        // 1. Handle Duplicate Emails (BAD_REQUEST)
        if (ex instanceof EmailAlreadyInUseException) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .build();
        }

        // 2. Handle Resource Not Found (NOT_FOUND)
        if (ex instanceof ResourceNotFoundException) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.NOT_FOUND)
                    .message(ex.getMessage())
                    .build();
        }

        // 3. Handle @Valid validation failures for Controller/Schema parameters
        if (ex instanceof HandlerMethodValidationException validationEx) {
            Map<String, String> errors = new HashMap<>();
            // Use getParameterValidationResults() instead of getAllValidationResults()
            validationEx.getParameterValidationResults().forEach(result -> {
                String argumentName = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(error -> {
                    errors.put(argumentName, error.getDefaultMessage());
                });
            });

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

            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.BAD_REQUEST)
                    .message("Validation Failed")
                    .extensions(Map.of("details", errors))
                    .build();
        }

        // 5. All other unexpected exceptions (INTERNAL_ERROR)
        // Log the actual exception trace here for internal debugging
        return GraphqlErrorBuilder.newError(env)
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("Unexpected error occurred")
                .build();
    }
}