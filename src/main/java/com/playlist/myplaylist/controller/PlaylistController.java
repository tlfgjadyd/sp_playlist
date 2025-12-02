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

/**
 * 사용자 플레이리스트와 관련된 HTTP 요청을 처리
 * 플레이리스트 생성, 보기, 트랙 추가, 플레이리스트 및 플레이리스트 내 트랙 삭제를 위한 엔드포인트를 제공
 * `PlaylistMapper` 및 `PlaylistTrackMapper`와 상호작용
 */
@Controller
@RequestMapping("/playlists") // playlists/...으로 매핑
public class PlaylistController {

    private final PlaylistMapper playlistMapper;
    private final PlaylistTrackMapper playlistTrackMapper;

    public PlaylistController(PlaylistMapper playlistMapper, PlaylistTrackMapper playlistTrackMapper) {
        this.playlistMapper = playlistMapper;
        this.playlistTrackMapper = playlistTrackMapper;
    }

    // 새 플레이리스트 생성 폼 제공
    @GetMapping("/create")
    public String showCreatePlaylistForm() {
        return "playlist-create"; // src/main/resources/templates/playlist-create.html
    }

    // 인증된 사용자를 위한 새 플레이리스트를 생성하기 위해 제공 받은 폼 내용을 처리
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

    // 인증된 사용자가 소유한 모든 플레이리스트 목록을 표시
    @GetMapping("/my")
    public String showMyPlaylists(Model model, @AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        int userId = customOAuth2User.getUser().getId();
        List<Playlist> playlists = playlistMapper.findByUserId(userId);
        model.addAttribute("playlists", playlists);
        return "playlist-my";
    }

    // 인증된 사용자의 플레이리스트를 JSON 형식으로 가져오기 위한 REST API 엔드포인트를 제공
    // 사용 이유 -> 원래는 새로운 폼을 이용해서 작성했는데 사용자 경험의 개선을 위해 플로팅 메세지를 이용하기 위해 사용 등...
    // @ResponsBody View 조회를 무시하고, HTTP message body에 직접 반환되는 string 값을 입력
    @GetMapping("/api/my")
    @ResponseBody
    public List<Playlist> getMyPlaylistsApi(@AuthenticationPrincipal CustomOAuth2UserService.CustomOAuth2User customOAuth2User) {
        int userId = customOAuth2User.getUser().getId();
        return playlistMapper.findByUserId(userId);
    }

    // 기존 플레이리스트에 트랙을 추가하기 위한 양식을 표시하며, 트랙 세부 정보를 미리 채움
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

    // 지정된 플레이리스트에 트랙을 추가하는 요청을 처리 중복 트랙 확인 로직을 포함하며, 성공 또는 실패를 나타내는 JSON 응답을 반환
    @PostMapping("/add-track")
    @ResponseBody
    public java.util.Map<String, Object> addTrackToPlaylist(@RequestParam int playlistId,
                                                            @RequestParam String trackId,
                                                            @RequestParam String trackName,
                                                            @RequestParam String artistName,
                                                            @RequestParam(required = false) String imageUrl) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();

        // 음악이 이미 플리안에 존재하는지 검사
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

    // 특정 플레이리스트의 세부 정보와 포함된 모든 트랙을 표시
    @GetMapping("/{playlistId}")
    public String showPlaylistDetail(@PathVariable int playlistId, Model model) {
        Playlist playlist = playlistMapper.findById(playlistId);
        List<PlaylistTrack> tracks = playlistTrackMapper.findByPlaylistId(playlistId);
        model.addAttribute("playlist", playlist);
        model.addAttribute("tracks", tracks);
        return "playlist-detail";
    }


    // 플레이리스트에서 특정 트랙을 삭제
    @PostMapping("/{playlistId}/tracks/{trackId}/delete")
    public String deleteTrackFromPlaylist(@PathVariable int playlistId,
                                          @PathVariable String trackId) {
        playlistTrackMapper.deleteByPlaylistIdAndTrackId(playlistId, trackId);
        return "redirect:/playlists/" + playlistId;
    }

    // 전체 플레이리스트와 연결된 모든 트랙을 삭제(플리 안에 음악 있어도 음악과 함께 삭제)
    @PostMapping("/{playlistId}/delete")
    public String deletePlaylist(@PathVariable int playlistId) {
        playlistMapper.deletePlaylistById(playlistId);
        return "redirect:/playlists/my";
    }
}
