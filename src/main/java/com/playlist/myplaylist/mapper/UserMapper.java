package com.playlist.myplaylist.mapper;

import com.playlist.myplaylist.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByEmail(String email);
    User findByUsername(String username);
    int insertUser(User user);
    int updateSpotifyTokens(User user);
}
