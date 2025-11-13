package com.playlist.myplaylist.service;

import com.playlist.myplaylist.mapper.UserMapper;
import com.playlist.myplaylist.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.data.browse.GetListOfNewReleasesRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.net.URI;
import java.time.Instant;

@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private URI redirectUri;

    private final UserMapper userMapper;

    // SpotifyApi 인스턴스는 더 이상 전역으로 하나만 사용하지 않고, 사용자별로 생성하거나 토큰을 설정하여 사용합니다.
    // 따라서 @PostConstruct init() 메소드에서 spotifyApi를 초기화하는 로직을 제거합니다.
    public SpotifyService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    // 사용자별 SpotifyApi 인스턴스를 생성하는 헬퍼 메소드
    private SpotifyApi getSpotifyApi(String accessToken, String refreshToken) {
        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .setAccessToken(accessToken)
                .setRefreshToken(refreshToken)
                .build();
    }

    // Client Credentials Flow를 위한 SpotifyApi 인스턴스 (공개 데이터 접근용)
    private SpotifyApi getClientCredentialsSpotifyApi() {
        SpotifyApi api = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
        try {
            ClientCredentials credentials = api.clientCredentials().build().execute();
            api.setAccessToken(credentials.getAccessToken());
        } catch (Exception e) {
            System.err.println("Error getting client credentials token: " + e.getMessage());
            e.printStackTrace();
        }
        return api;
    }

    public URI authorizationCodeUri() {
        SpotifyApi tempSpotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();

        AuthorizationCodeUriRequest authorizationCodeUriRequest = tempSpotifyApi.authorizationCodeUri()
                .scope("user-read-private user-read-email playlist-read-private playlist-read-collaborative") // 필요한 스코프 추가
                .build();
        return authorizationCodeUriRequest.execute();
    }

    public void authorizationCode(String code, User user) {
        try {
            SpotifyApi tempSpotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectUri(redirectUri)
                    .build();

            AuthorizationCodeRequest authorizationCodeRequest = tempSpotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

            user.setSpotifyAccessToken(credentials.getAccessToken());
            user.setSpotifyRefreshToken(credentials.getRefreshToken());
            // 토큰 만료 시간도 저장할 수 있지만, 여기서는 간단히 액세스/리프레시 토큰만 저장
            userMapper.updateSpotifyTokens(user);

            System.out.println("Access Token for user " + user.getUsername() + ": " + credentials.getAccessToken());
            System.out.println("Refresh Token for user " + user.getUsername() + ": " + credentials.getRefreshToken());

        } catch (Exception e) {
            System.err.println("Error getting tokens: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 리프레시 토큰으로 액세스 토큰을 갱신하는 메소드
    public String refreshAccessToken(User user) {
        try {
            SpotifyApi tempSpotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectUri(redirectUri)
                    .setRefreshToken(user.getSpotifyRefreshToken())
                    .build();

            AuthorizationCodeRefreshRequest authorizationCodeRefreshRequest = tempSpotifyApi.authorizationCodeRefresh().build();
            AuthorizationCodeCredentials credentials = authorizationCodeRefreshRequest.execute();

            user.setSpotifyAccessToken(credentials.getAccessToken());
            // 리프레시 토큰은 보통 바뀌지 않지만, 혹시 바뀌면 업데이트
            if (credentials.getRefreshToken() != null) {
                user.setSpotifyRefreshToken(credentials.getRefreshToken());
            }
            userMapper.updateSpotifyTokens(user);

            System.out.println("Access Token refreshed for user " + user.getUsername() + ": " + credentials.getAccessToken());
            return credentials.getAccessToken();

        } catch (Exception e) {
            System.err.println("Error refreshing access token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 사용자 토큰으로 SpotifyApi 인스턴스를 얻는 메소드 (토큰 갱신 로직 포함)
    public SpotifyApi getSpotifyApiForUser(User user) {
        SpotifyApi api = getSpotifyApi(user.getSpotifyAccessToken(), user.getSpotifyRefreshToken());

        // TODO: 실제 만료 시간을 DB에 저장하고 비교하는 로직으로 개선 필요
        // 현재는 API 호출 시 에러가 발생하면 토큰 갱신을 시도하는 방식으로 구현
        try {
            // 간단한 API 호출로 토큰 유효성 검사 (예: 사용자 프로필 조회)
            api.getCurrentUsersProfile().build().execute();
        } catch (Exception e) {
            // 토큰 만료 등 오류 발생 시 토큰 갱신 시도
            System.err.println("User access token might be expired or invalid. Attempting to refresh...");
            String newAccessToken = refreshAccessToken(user);
            if (newAccessToken != null) {
                api.setAccessToken(newAccessToken);
                System.out.println("Access token successfully refreshed for user: " + user.getUsername());
            } else {
                System.err.println("Failed to refresh access token for user: " + user.getUsername());
                // 토큰 갱신 실패 시, 사용자에게 재연결을 요청해야 함
                return null;
            }
        }
        return api;
    }

    public AlbumSimplified[] getNewReleases() {
        try {
            SpotifyApi api = getClientCredentialsSpotifyApi(); // Client Credentials Flow 사용
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

    public Playlist getGlobalTopPlaylist(User user) {
        try {
            SpotifyApi api = getSpotifyApiForUser(user); // 사용자 토큰으로 API 인스턴스 생성
            if (api == null) {
                System.err.println("Failed to get SpotifyApi instance for user. Token refresh failed or user not connected.");
                return null;
            }
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

    public Paging<Track> searchTracks() {
        try {
            SpotifyApi api = getClientCredentialsSpotifyApi(); // Client Credentials Flow 사용
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