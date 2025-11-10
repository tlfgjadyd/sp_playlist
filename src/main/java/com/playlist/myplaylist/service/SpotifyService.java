package com.playlist.myplaylist.service;

import com.neovisionaries.i18n.CountryCode;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.browse.GetListOfNewReleasesRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import java.net.URI;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private URI redirectUri;

    private SpotifyApi spotifyApi;

    @PostConstruct
    public void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();
    }

    public URI authorizationCodeUri() {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("user-read-private user-read-email playlist-read-private")
                .build();
        return authorizationCodeUriRequest.execute();
    }

    public void authorizationCode(String code) {
        try {
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            var authorizationCodeCredentials = authorizationCodeRequest.execute();

            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());

            System.out.println("Access Token Expires In: " + authorizationCodeCredentials.getExpiresIn());

        } catch (Exception e) {
            System.err.println("Error getting tokens: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public AlbumSimplified[] getNewReleases() {
        try {
            GetListOfNewReleasesRequest getListOfNewReleasesRequest = spotifyApi.getListOfNewReleases()
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

    public Playlist getGlobalTopPlaylist() {
        try {
            String playlistId = "37i9dQZEVXbLRQDuF5jeBp";

            GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId)
                    .build();

            return getPlaylistRequest.execute();
        } catch (Exception e) {
            System.err.println("Error getting global top playlist: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Paging<Track> searchTracks() {
        try {
            SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks("love")
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