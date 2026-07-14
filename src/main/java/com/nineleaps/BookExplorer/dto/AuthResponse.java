package com.nineleaps.BookExplorer.dto;

import com.nineleaps.BookExplorer.entity.User;

public record AuthResponse(String token, User user) {}