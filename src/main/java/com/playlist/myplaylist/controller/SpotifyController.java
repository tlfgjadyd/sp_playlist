package com.playlist.myplaylist.controller;

import com.playlist.myplaylist.service.CustomOAuth2UserService;
import com.playlist.myplaylist.service.SpotifyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;

@Controller
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/new-releases")
    public String getNewReleases(Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        String accessToken = customOAuth2User.getUser().getSpotifyAccessToken();
        AlbumSimplified[] newReleases = spotifyService.getNewReleases(accessToken);
        model.addAttribute("newReleases", newReleases);
        return "new-releases";
    }

    @GetMapping("/global-top")
    public String getGlobalTop(Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        String accessToken = customOAuth2User.getUser().getSpotifyAccessToken();
        Playlist playlist = spotifyService.getGlobalTopPlaylist(accessToken);
        model.addAttribute("playlist", playlist);
        return "global-top";
    }

    @GetMapping("/search-test")
    public String searchTest(Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        String accessToken = customOAuth2User.getUser().getSpotifyAccessToken();
        Paging<Track> trackPaging = spotifyService.searchTracks(accessToken);
        model.addAttribute("trackPaging", trackPaging);
        return "search-results";
    }

}
