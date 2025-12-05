package com.playlist.myplaylist.mapper;

import com.playlist.myplaylist.model.Playlist;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlaylistMapper {
    int insertPlaylist(Playlist playlist);
    List<Playlist> findByUserId(int userId);
    Playlist findById(int id);
    int updatePlaylist(Playlist playlist);
    int deletePlaylistById(int id);
}
