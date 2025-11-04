package com.playlist.myplaylist.controller;

import com.playlist.myplaylist.service.SpotifyService;
import com.playlist.myplaylist.service.SpotifyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;

@Controller
public class SpotifyController {

    private final SpotifyService spotifyService;

    public SpotifyController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/new-releases")
    public String getNewReleases(Model model) {
        AlbumSimplified[] newReleases = spotifyService.getNewReleases();
        model.addAttribute("newReleases", newReleases);
        return "new-releases";
    }
}
