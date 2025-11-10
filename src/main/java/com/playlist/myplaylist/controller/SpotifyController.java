package com.playlist.myplaylist.controller;

import com.playlist.myplaylist.service.SpotifyService;
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

    @GetMapping("/login")
    public String login() {
        URI uri = spotifyService.authorizationCodeUri();
        return "redirect:" + uri.toString();
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code) {
        spotifyService.authorizationCode(code);
        return "redirect:/global-top";
    }

    @GetMapping("/new-releases")
    public String getNewReleases(Model model) {
        AlbumSimplified[] newReleases = spotifyService.getNewReleases();
        model.addAttribute("newReleases", newReleases);
        return "new-releases";
    }

    @GetMapping("/global-top")
    public String getGlobalTop(Model model) {
        Playlist playlist = spotifyService.getGlobalTopPlaylist();
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
