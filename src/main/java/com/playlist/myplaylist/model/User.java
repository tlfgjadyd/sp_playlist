package com.playlist.myplaylist.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Setter
@Getter
public class User {
    // Getters and Setters
    private int id;
    private String username;
    private String email;
    private String spotifyUserId;
    private Timestamp createdAt;
    private String spotifyAccessToken;
    private String spotifyRefreshToken;
    private LocalDateTime spotifyAccessTokenExpiresAt;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", spotifyUserId='" + spotifyUserId + '\'' +
                ", createdAt=" + createdAt +
                ", spotifyAccessToken='" + spotifyAccessToken + '\'' +
                ", spotifyRefreshToken='" + spotifyRefreshToken + '\'' +
                ", spotifyAccessTokenExpiresAt=" + spotifyAccessTokenExpiresAt +
                '}';
    }
}
