package com.playlist.myplaylist.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class PlaylistTrack {
    // Getters and Setters
    private int id;
    private int playlistId;
    private String trackId;
    private String trackName;
    private String artistName;
//    private String previewUrl;
    private String imageUrl;
    private Timestamp createdAt;

    @Override
    public String toString() {
        return "PlaylistTrack{" +
                "id=" + id +
                ", playlistId=" + playlistId +
                ", trackId='" + trackId + "'" +
                ", trackName='" + trackName + "'" +
                ", artistName='" + artistName + "'" +
//                ", previewUrl='" + previewUrl + "'" +
                ", imageUrl='" + imageUrl + "'" +
                ", createdAt=" + createdAt +
                '}';
    }
}
