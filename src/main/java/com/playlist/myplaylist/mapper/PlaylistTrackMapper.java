package com.playlist.myplaylist.mapper;

import com.playlist.myplaylist.model.PlaylistTrack;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlaylistTrackMapper {
    int insertPlaylistTrack(PlaylistTrack playlistTrack);
    List<PlaylistTrack> findByPlaylistId(int playlistId);
    int existsByPlaylistIdAndTrackId(@Param("playlistId") int playlistId, @Param("trackId") String trackId);
    int deleteByPlaylistIdAndTrackId(@Param("playlistId") int playlistId, @Param("trackId") String trackId);
}
