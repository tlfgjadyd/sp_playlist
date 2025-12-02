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

/**
 * Spotify 웹 API와 상호작용하기 위한 인터페이스를 제공
 * Spotify API 인증(토큰 갱신) 및 데이터 검색 작업을 처리
 */
@Service
public class SpotifyService {

    private final SpotifyApi spotifyApi;
    private final UserMapper userMapper;

    public SpotifyService(SpotifyApi spotifyApi, UserMapper userMapper) {
        this.spotifyApi = spotifyApi;
        this.userMapper = userMapper;
    }
    // 유저 초기화
    private SpotifyApi getInitializedSpotifyApi(User user) {
        // 1. 토큰 만료 여부 확인: 만료 시간이 현재 시간보다 1분 이상 이전인지 확인하여 갱신이 필요한지 판단
        if (user.getSpotifyAccessTokenExpiresAt() != null &&
                user.getSpotifyAccessTokenExpiresAt().isBefore(LocalDateTime.now().minusMinutes(1))) {
            try {
                // 2. Refresh Token을 사용하여 새로운 Access Token 요청
                spotifyApi.setRefreshToken(user.getSpotifyRefreshToken());
                // 토큰 갱신 실행
                AuthorizationCodeCredentials credentials = spotifyApi.authorizationCodeRefresh().build().execute();
                // 3. 새로운 토큰 정보 업데이트
                user.setSpotifyAccessToken(credentials.getAccessToken());
                // 새 만료 시간 계산 (현재 시간 + Spotify가 지정한 만료 시간(초))
                user.setSpotifyAccessTokenExpiresAt(LocalDateTime.now().plusSeconds(credentials.getExpiresIn()));
                // 4. 데이터베이스에 새로운 Access Token 및 만료 시간 업데이트
                userMapper.updateSpotifyAccessToken(user);
                // 5. SpotifyApi 인스턴스에 새로운 Access Token 설정
                spotifyApi.setAccessToken(credentials.getAccessToken());
            } catch (Exception e) {
                // 토큰 갱신 실패 시 에러 처리 및 로깅
                System.err.println("Error refreshing access token: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // 토큰이 만료되지 않았다면 기존 Access Token을 사용하여 API 클라이언트 설정
            spotifyApi.setAccessToken(user.getSpotifyAccessToken());
        }
        return spotifyApi;
    }

    /**
     * 새 발매 앨범을 페이징 형태로 조회
     * @param user spotify 사용자
     * @param limit 한 번에 가져올 앨범 개수
     */
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

    /**
     * 현재 사용자가 가장 많이 들은 곡을 출력(Spotify에서)
     * @param user 사용자
     */
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

    /**
     * 검색
     * @param user 사용자
     * @param query 검색 쿼리
     */
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

    /**
     * 앨범의 정보를 가져옴
     * @param user 사용자
     * @param albumId 앨범 ID
     */
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

    /**
     * 앨범 안의 곡(track)정보를 가져옴
     * @param user 사용자
     * @param albumId 앨범 ID
     */
    public Paging<TrackSimplified> getAlbumTracks(User user, String albumId) {
        try {
            SpotifyApi api = getInitializedSpotifyApi(user);
            GetAlbumsTracksRequest getAlbumsTracksRequest = api.getAlbumsTracks(albumId)
                    .limit(50)
                    .build();
            return getAlbumsTracksRequest.execute();
        } catch (Exception e) {
            System.err.println("Error getting album tracks: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}