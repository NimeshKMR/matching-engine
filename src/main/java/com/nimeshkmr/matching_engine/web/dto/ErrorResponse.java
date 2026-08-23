package com.nimeshkmr.matching_engine.web.dto;

public record ErrorResponse(
    int status,
    String error,
    String message
) {
}