package com.playlist.myplaylist.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;

/**
 * SpotifyApi의 싱글톤 인스턴스를 생성하고 제공하는 구성 클래스
 */
@Configuration
public class SpotifyApiConfig {
    // yml -> spotify.client-id
    @Value("${spotify.client-id}")
    private String clientId;
    // yml -> spotify.client-sec
    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Bean
    public SpotifyApi spotifyApi() {
        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }
}
