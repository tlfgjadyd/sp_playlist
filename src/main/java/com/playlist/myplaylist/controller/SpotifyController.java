package com.playlist.myplaylist.controller;

import com.playlist.myplaylist.model.User;
import com.playlist.myplaylist.service.CustomOAuth2UserService;
import com.playlist.myplaylist.service.SpotifyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
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
        User user = customOAuth2User.getUser();
        AlbumSimplified[] newReleases = spotifyService.getNewReleases(user);
        model.addAttribute("newReleases", newReleases);
        return "new-releases";
    }

    @GetMapping("/top-tracks")
    public String getTopTracks(Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        Paging<Track> trackPaging = spotifyService.getUsersTopTracks(user);
        model.addAttribute("trackPaging", trackPaging);
        return "top-tracks";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false, defaultValue = "love") String query, Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        User user = customOAuth2User.getUser();
        Paging<Track> trackPaging = spotifyService.searchTracks(user, query);
        model.addAttribute("trackPaging", trackPaging);
        model.addAttribute("query", query);
        return "search-results";
    }

}
