package com.playlist.myplaylist.service;

import com.playlist.myplaylist.mapper.UserMapper;
import com.playlist.myplaylist.model.User;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumsTracksRequest;
import se.michaelthelin.spotify.requests.data.browse.GetListOfNewReleasesRequest;
import se.michaelthelin.spotify.requests.data.personalization.simplified.GetUsersTopTracksRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.time.LocalDateTime;

@Service
public class SpotifyService {

    private final SpotifyApi spotifyApi;
    private final UserMapper userMapper;

    public SpotifyService(SpotifyApi spotifyApi, UserMapper userMapper) {
        this.spotifyApi = spotifyApi;
        this.userMapper = userMapper;
    }

    private SpotifyApi getInitializedSpotifyApi(User user) {
        if (user.getSpotifyAccessTokenExpiresAt() != null &&
                user.getSpotifyAccessTokenExpiresAt().isBefore(LocalDateTime.now().minusMinutes(1))) {
            try {
                spotifyApi.setRefreshToken(user.getSpotifyRefreshToken());
                AuthorizationCodeCredentials credentials = spotifyApi.authorizationCodeRefresh().build().execute();

                user.setSpotifyAccessToken(credentials.getAccessToken());
                user.setSpotifyAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(credentials.getExpiresIn()));
                userMapper.updateSpotifyAccessToken(user);

                spotifyApi.setAccessToken(credentials.getAccessToken());
            } catch (Exception e) {
                System.err.println("Error refreshing access token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            spotifyApi.setAccessToken(user.getSpotifyAccessToken());
        }
        return spotifyApi;
    }


    public Paging<AlbumSimplified> getNewReleases(User user, int limit, int offset) {
        try {
            SpotifyApi api = getInitializedSpotifyApi(user);
            GetListOfNewReleasesRequest getListOfNewReleasesRequest = api.getListOfNewReleases()
                    .limit(limit)
                    .offset(offset)
                    .build();

            return getListOfNewReleasesRequest.execute();
        } catch (Exception e) {
            System.err.println("Error getting new releases: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Paging<Track> getUsersTopTracks(User user) {
        try {
            SpotifyApi api = getInitializedSpotifyApi(user);
            GetUsersTopTracksRequest getUsersTopTracksRequest = api.getUsersTopTracks()
                    .limit(10)
                    .time_range("medium_term")
                    .build();
            return getUsersTopTracksRequest.execute();
        } catch (Exception e) {
            System.err.println("Error getting user's top tracks: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Paging<Track> searchTracks(User user, String query, int offset) {
        try {
            SpotifyApi api = getInitializedSpotifyApi(user);
            SearchTracksRequest searchTracksRequest = api.searchTracks(query)
                    .limit(20) // Increase limit for better infinite scroll experience
                    .offset(offset)
                    .build();
            return searchTracksRequest.execute();
        } catch (Exception e) {
            System.err.println("Error searching tracks: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Album getAlbum(User user, String albumId) {
        try {
            SpotifyApi api = getInitializedSpotifyApi(user);
            GetAlbumRequest getAlbumRequest = api.getAlbum(albumId).build();
            return getAlbumRequest.execute();
        } catch (Exception e) {
            System.err.println("Error getting album: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Paging<TrackSimplified> getAlbumTracks(User user, String albumId) {
        try {
            SpotifyApi api = getInitializedSpotifyApi(user);
            GetAlbumsTracksRequest getAlbumsTracksRequest = api.getAlbumsTracks(albumId)
                    .limit(50) // Get up to 50 tracks
                    .build();
            return getAlbumsTracksRequest.execute();
        } catch (Exception e) {
            System.err.println("Error getting album tracks: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}