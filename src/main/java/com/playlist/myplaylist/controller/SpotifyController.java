package com.playlist.myplaylist.controller;

import com.playlist.myplaylist.service.CustomUserDetailsService;
import com.playlist.myplaylist.service.SpotifyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.net.URI;

@Controller
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/connect-spotify")
    public String connectSpotify(@AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails currentUser) {
        if (currentUser == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }
        URI uri = spotifyService.authorizationCodeUri();
        return "redirect:" + uri.toString();
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code,
                           @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails currentUser) {
        if (currentUser == null) {
            return "redirect:/login"; // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
        }
        spotifyService.authorizationCode(code, currentUser.getUser());
        return "redirect:/global-top"; // 토큰 저장 후 글로벌 탑 페이지로 리다이렉트
    }

    @GetMapping("/new-releases")
    public String getNewReleases(Model model) {
        AlbumSimplified[] newReleases = spotifyService.getNewReleases();
        model.addAttribute("newReleases", newReleases);
        return "new-releases";
    }

    @GetMapping("/global-top")
    public String getGlobalTop(Model model, @AuthenticationPrincipal CustomUserDetailsService.CustomUserDetails currentUser) {
        if (currentUser == null || currentUser.getUser().getSpotifyAccessToken() == null) {
            // Spotify 토큰이 없으면 연결 페이지로 리다이렉트
            return "redirect:/connect-spotify";
        }
        Playlist playlist = spotifyService.getGlobalTopPlaylist(currentUser.getUser());
        model.addAttribute("playlist", playlist);
        return "global-top";
    }

    @GetMapping("/search-test")
    public String searchTest(Model model) {
        Paging<Track> trackPaging = spotifyService.searchTracks();
        model.addAttribute("trackPaging", trackPaging);
        return "search-results";
    }
}
