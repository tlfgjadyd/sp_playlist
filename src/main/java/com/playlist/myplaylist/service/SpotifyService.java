package com.playlist.myplaylist.service;

import com.playlist.myplaylist.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.browse.GetListOfNewReleasesRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private SpotifyApi getSpotifyApi(String accessToken) {
        return new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .build();
    }

    public AlbumSimplified[] getNewReleases(String accessToken) {
        try {
            SpotifyApi api = getSpotifyApi(accessToken);
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

    public Playlist getGlobalTopPlaylist(String accessToken) {
        try {
            SpotifyApi api = getSpotifyApi(accessToken);
            String playlistId = "37i9dQZEVXbLRQDuF5jeBp"; // Global Top 50

            GetPlaylistRequest getPlaylistRequest = api.getPlaylist(playlistId)
                    .build();

            return getPlaylistRequest.execute();
        } catch (Exception e) {
            System.err.println("Error getting global top playlist: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Paging<Track> searchTracks(String accessToken) {
        try {
            SpotifyApi api = getSpotifyApi(accessToken);
            SearchTracksRequest searchTracksRequest = api.searchTracks("love")
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