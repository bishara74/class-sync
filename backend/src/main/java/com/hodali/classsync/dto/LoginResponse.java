package com.hodali.classsync.dto;

import com.hodali.classsync.model.User;

public record LoginResponse(String token, User user) {
}
