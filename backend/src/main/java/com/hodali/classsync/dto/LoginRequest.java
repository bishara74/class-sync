package com.hodali.classsync.dto;

public record LoginRequest(
        String email,
        String password,
        String neptunCode
) {}