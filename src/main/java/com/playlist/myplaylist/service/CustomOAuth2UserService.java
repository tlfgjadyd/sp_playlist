package com.playlist.myplaylist.service;

import com.playlist.myplaylist.mapper.UserMapper;
import com.playlist.myplaylist.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;

    public CustomOAuth2UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String spotifyUserId = (String) attributes.get("id");

        User user = userMapper.findBySpotifyUserId(spotifyUserId);

        if (user == null) {
            // New Spotify user.
            String username = (String) attributes.get("display_name");
            String email = (String) attributes.get("email");

            // Handle potential username collision or null username
            if (username == null || username.isBlank() || userMapper.findByUsername(username) != null) {
                username = "user_" + spotifyUserId;
            }

            // Handle potential email collision or null email
            if (email == null || email.isBlank() || userMapper.findByEmail(email) != null) {
                email = spotifyUserId + "@myplaylist.app"; // Create a placeholder email
            }

            user = new User();
            user.setSpotifyUserId(spotifyUserId);
            user.setUsername(username);
            user.setEmail(email);
        }

        return new CustomOAuth2User(oAuth2User, user);
    }

    public static class CustomOAuth2User implements OAuth2User {
        private final OAuth2User oAuth2User;
        @Getter
        private final User user;

        public CustomOAuth2User(OAuth2User oAuth2User, User user) {
            this.oAuth2User = oAuth2User;
            this.user = user;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return oAuth2User.getAttributes();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return oAuth2User.getAuthorities();
        }

        @Override
        public String getName() {
            return oAuth2User.getAttribute("display_name");
        }

    }
}
