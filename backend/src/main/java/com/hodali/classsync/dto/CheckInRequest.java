package com.hodali.classsync.dto;

public record CheckInRequest(
        Long studentId,
        String code
) {}