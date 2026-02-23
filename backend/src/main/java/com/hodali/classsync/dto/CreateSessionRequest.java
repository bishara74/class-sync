package com.hodali.classsync.dto;

public record CreateSessionRequest(
        Long teacherId,
        String courseName,
        int validForMinutes
) {}