package com.playlist.myplaylist.mapper;

import com.playlist.myplaylist.model.PlaylistTrack;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlaylistTrackMapper {
    int insertPlaylistTrack(PlaylistTrack playlistTrack);
    List<PlaylistTrack> findByPlaylistId(int playlistId);
}
