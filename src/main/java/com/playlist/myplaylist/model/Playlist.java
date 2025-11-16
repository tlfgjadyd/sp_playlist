package com.playlist.myplaylist.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class Playlist {
    // Getters and Setters
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
