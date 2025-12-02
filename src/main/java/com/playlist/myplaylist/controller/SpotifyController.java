package com.playlist.myplaylist.controller;

import com.playlist.myplaylist.model.TrackViewModel;
import com.playlist.myplaylist.model.User;
import com.playlist.myplaylist.service.CustomOAuth2UserService;
import com.playlist.myplaylist.service.SpotifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;
import se.michaelthelin.spotify.model_objects.specification.*;
import com.playlist.myplaylist.model.TrackViewModel;
import org.springframework.web.util.UriComponentsBuilder;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

/**
 * Spotify 기능과 관련된 HTTP 요청을 처리
 * `SpotifyService`를 통해 Spotify API에서 데이터를 가져와 다양한 HTML 템플릿에 표시할 수 있도록 준비
 */
@Controller
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    // 로그인
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    // 새로 발매된 앨범 조회
    @GetMapping("/new-releases")
    public String getNewReleases(Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        Paging<AlbumSimplified> paging = spotifyService.getNewReleases(user, 12, 0);
        model.addAttribute("paging", paging);
        return "new-releases";
    }

    // 계정 사용자가 Spotify에서 최근에 많이 청취한 곡 목록
    @GetMapping("/top-tracks")
    public String getTopTracks(Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        Paging<Track> trackPaging = spotifyService.getUsersTopTracks(user);
        model.addAttribute("trackPaging", trackPaging);
        return "top-tracks";
    }
    // 검색
    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false) String query, Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        if (query != null && !query.isBlank()) {
            User user = customOAuth2User.getUser();
            // Initial search with offset 0
            Paging<Track> trackPaging = spotifyService.searchTracks(user, query, 0);
            model.addAttribute("trackPaging", trackPaging);
        }
        model.addAttribute("query", query);
        return "search-results";
    }
    // 검색 쿼리 요청
    @GetMapping("/api/search")
    @ResponseBody
    public Paging<Track> searchApi(@RequestParam(name = "q") String query,
                                   @RequestParam(name = "offset", defaultValue = "0") int offset,
                                   @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        return spotifyService.searchTracks(user, query, offset);
    }

    // new-release 무한 스크롤 위한 api요청
    @GetMapping("/api/new-releases")
    @ResponseBody // JSON으로 반환
    public ResponseEntity<Paging<AlbumSimplified>> getNewReleasesApi(
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {

        User user = customOAuth2User.getUser();
        Paging<AlbumSimplified> paging = spotifyService.getNewReleases(user, limit, offset);

        return ResponseEntity.ok(paging);
    }



    // 새로 발매된 앨범의 세부정보를 얻음
    @GetMapping("/album/{id}")
    public String showAlbumDetails(@PathVariable String id, Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        Album album = spotifyService.getAlbum(user, id);
        Paging<TrackSimplified> tracksPaging = spotifyService.getAlbumTracks(user, id);

        model.addAttribute("album", album);

        if (tracksPaging != null) {
            java.util.List<TrackViewModel> trackViewModels = new java.util.ArrayList<>();
            for (TrackSimplified track : tracksPaging.getItems()) {
                // Format duration
                String duration = "N/A";
                if (track.getDurationMs() != null) {
                    long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(track.getDurationMs());
                    long seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(track.getDurationMs()) % 60;
                    duration = String.format("%d:%02d", minutes, seconds);
                }

                // Format artists
                java.util.stream.Stream<ArtistSimplified> artistStream = java.util.Arrays.stream(track.getArtists());
                String artists = artistStream
                                    .map(ArtistSimplified::getName)
                                    .collect(java.util.stream.Collectors.joining(", "));

                // Build URL
                String imageUrl = (album.getImages() != null && album.getImages().length > 0) ? album.getImages()[0].getUrl() : "";


                trackViewModels.add(new TrackViewModel(
                        track.getId(),
                        track.getName(),
                        artists,
                        duration,
                        track.getTrackNumber()
                ));
            }
            model.addAttribute("tracks", trackViewModels);
        } else {
            model.addAttribute("tracks", new java.util.ArrayList<>());
        }

        return "album-detail";
    }
}