package com.playlist.myplaylist.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * 플레이리스트 정보를 나타내기위한 모델
 */
@Setter
@Getter
public class Playlist {

    private int id;
    private int userId;
    private String name;
    private String description;
    private Timestamp createdAt;

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + "'" +
                ", description='" + description + "'" +
                ", createdAt=" + createdAt +
                '}';
    }
}
