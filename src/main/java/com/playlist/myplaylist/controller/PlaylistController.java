package com.playlist.myplaylist.controller;

import com.playlist.myplaylist.mapper.PlaylistMapper;
import com.playlist.myplaylist.mapper.PlaylistTrackMapper;
import com.playlist.myplaylist.model.Playlist;
import com.playlist.myplaylist.model.PlaylistTrack;
import com.playlist.myplaylist.service.CustomOAuth2UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistMapper playlistMapper;
    private final PlaylistTrackMapper playlistTrackMapper;

    public PlaylistController(PlaylistMapper playlistMapper, PlaylistTrackMapper playlistTrackMapper) {
        this.playlistMapper = playlistMapper;
        this.playlistTrackMapper = playlistTrackMapper;
    }

    @GetMapping("/create")
    public String showCreatePlaylistForm() {
        return "playlist-create"; // src/main/resources/templates/playlist-create.html
    }

    @PostMapping("/create")
    public String createPlaylist(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setDescription(description);
        playlist.setUserId(customOAuth2User.getUser().getId()); // 현재 로그인한 사용자의 ID 설정

        playlistMapper.insertPlaylist(playlist);

        return "redirect:/playlists/my"; // 내 플레이리스트 목록 페이지로 리다이렉트
    }

    @GetMapping("/my")
    public String showMyPlaylists(Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        int userId = customOAuth2User.getUser().getId();
        List<Playlist> playlists = playlistMapper.findByUserId(userId);
        model.addAttribute("playlists", playlists);
        return "playlist-my";
    }

    @GetMapping("/api/my")
    @ResponseBody
    public List<Playlist> getMyPlaylistsApi(@AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        int userId = customOAuth2User.getUser().getId();
        return playlistMapper.findByUserId(userId);
    }

    @GetMapping("/add-track")
    public String showAddTrackToPlaylistForm(@RequestParam String trackId,
                                             @RequestParam String trackName,
                                             @RequestParam String artistName,
                                             @RequestParam(required = false) String imageUrl,
                                             Model model,
                                             @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        int userId = customOAuth2User.getUser().getId();
        List<Playlist> playlists = playlistMapper.findByUserId(userId);

        model.addAttribute("trackId", trackId);
        model.addAttribute("trackName", trackName);
        model.addAttribute("artistName", artistName);
        model.addAttribute("imageUrl", imageUrl);
        model.addAttribute("playlists", playlists);

        return "playlist-add-track";
    }

    @PostMapping("/add-track")
    @ResponseBody
        public java.util.Map<String, Object> addTrackToPlaylist(@RequestParam int playlistId,
                                                                @RequestParam String trackId,
                                                                @RequestParam String trackName,
                                                                @RequestParam String artistName,
                                                                @RequestParam(required = false) String imageUrl) {
            java.util.Map<String, Object> response = new java.util.HashMap<>();
    
            // Check if the track already exists in the playlist
            int count = playlistTrackMapper.existsByPlaylistIdAndTrackId(playlistId, trackId);
            if (count > 0) {
                response.put("success", false);
                response.put("message", "Track already exists in this playlist.");
                return response;
            }
    
            PlaylistTrack playlistTrack = new PlaylistTrack();
            playlistTrack.setPlaylistId(playlistId);
            playlistTrack.setTrackId(trackId);
            playlistTrack.setTrackName(trackName);
            playlistTrack.setArtistName(artistName);
            playlistTrack.setImageUrl(imageUrl);
    
            playlistTrackMapper.insertPlaylistTrack(playlistTrack);
    
            Playlist playlist = playlistMapper.findById(playlistId);
    
            response.put("success", true);
            response.put("playlistId", playlistId);
            response.put("playlistName", playlist.getName());
            return response;
        }

        @GetMapping("/{playlistId}")
        public String showPlaylistDetail(@PathVariable int playlistId, Model model) {
            Playlist playlist = playlistMapper.findById(playlistId);
            List<PlaylistTrack> tracks = playlistTrackMapper.findByPlaylistId(playlistId);
            model.addAttribute("playlist", playlist);
            model.addAttribute("tracks", tracks);
            return "playlist-detail";
        }

    

        @PostMapping("/{playlistId}/tracks/{trackId}/delete")
            public String deleteTrackFromPlaylist(@PathVariable int playlistId,
                                                  @PathVariable String trackId) {
                playlistTrackMapper.deleteByPlaylistIdAndTrackId(playlistId, trackId);
                return "redirect:/playlists/" + playlistId;
            }
        
            @PostMapping("/{playlistId}/delete")
            public String deletePlaylist(@PathVariable int playlistId) {
                playlistMapper.deletePlaylistById(playlistId);
                return "redirect:/playlists/my";
            }
        }
