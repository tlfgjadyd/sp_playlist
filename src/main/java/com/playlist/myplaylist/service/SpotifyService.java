package com.playlist.myplaylist.service;

import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.requests.data.browse.GetListOfNewReleasesRequest;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private SpotifyApi spotifyApi;

    @PostConstruct
    public void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
        clientCredentials_Sync();
    }

    public void clientCredentials_Sync() {
        try {
            var clientCredentialsRequest = spotifyApi.clientCredentials().build();
            var clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            System.out.println("Access token expires in: " + clientCredentials.getExpiresIn());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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
            System.out.println("Error: " + e.getMessage());
            return new AlbumSimplified[0];
        }
    }
}