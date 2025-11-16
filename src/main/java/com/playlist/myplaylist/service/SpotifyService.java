package com.playlist.myplaylist.service;

import com.playlist.myplaylist.mapper.UserMapper;
import com.playlist.myplaylist.model.User;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
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


    public AlbumSimplified[] getNewReleases(User user) {
        try {
            SpotifyApi api = getInitializedSpotifyApi(user);
            GetListOfNewReleasesRequest getListOfNewReleasesRequest = api.getListOfNewReleases()
                    .limit(10)
                    .build();

            Paging<AlbumSimplified> albumSimplifiedPaging = getListOfNewReleasesRequest.execute();
            return albumSimplifiedPaging.getItems();
        } catch (Exception e) {
            System.err.println("Error getting new releases: " + e.getMessage());
            e.printStackTrace();
            return new AlbumSimplified[0];
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

    public Paging<Track> searchTracks(User user, String query) {
        try {
            SpotifyApi api = getInitializedSpotifyApi(user);
            SearchTracksRequest searchTracksRequest = api.searchTracks(query)
                    .limit(10)
                    .build();
            return searchTracksRequest.execute();
        } catch (Exception e) {
            System.err.println("Error searching tracks: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}