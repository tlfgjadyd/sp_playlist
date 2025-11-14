package com.playlist.myplaylist.mapper;

import com.playlist.myplaylist.model.PlaylistTrack;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlaylistTrackMapper {
    int insertPlaylistTrack(PlaylistTrack playlistTrack);
    // We will add methods like findByPlaylistId, insertTrack, etc. here later.
}
