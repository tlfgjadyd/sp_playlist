package com.playlist.myplaylist.mapper;

import com.playlist.myplaylist.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findBySpotifyUserId(String spotifyUserId);
    User findByUsername(String username);
    User findByEmail(String email);
    int insertUser(User user);
    int updateSpotifyTokens(User user);
    int updateSpotifyAccessToken(User user);
}
