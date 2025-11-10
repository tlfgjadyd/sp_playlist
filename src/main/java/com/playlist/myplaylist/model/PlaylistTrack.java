package com.playlist.myplaylist.model;

import java.sql.Timestamp;

public class PlaylistTrack {
    private int id;
    private int playlistId;
    private String trackId;
    private String trackName;
    private String artistName;
    private String previewUrl;
    private String imageUrl;
    private Timestamp createdAt;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PlaylistTrack{" +
                "id=" + id +
                ", playlistId=" + playlistId +
                ", trackId='" + trackId + "'" +
                ", trackName='" + trackName + "'" +
                ", artistName='" + artistName + "'" +
                ", previewUrl='" + previewUrl + "'" +
                ", imageUrl='" + imageUrl + "'" +
                ", createdAt=" + createdAt +
                '}';
    }
}
