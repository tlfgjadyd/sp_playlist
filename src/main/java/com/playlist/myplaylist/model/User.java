package com.playlist.myplaylist.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * 애플리케이션의 사용자 정보를 담는 데이터 모델 클래스
 * 데이터베이스의 'users' 테이블과 매핑
 */
@Setter
@Getter
public class User {
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
