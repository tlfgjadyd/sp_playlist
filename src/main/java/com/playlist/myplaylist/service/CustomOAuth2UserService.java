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

/**
 * Spotify를 사용한 OAuth2 사용자 정보 로딩을 담당하는 커스텀 서비스
 * OAuth2 인증 후, Spotify API에서 받은 사용자 정보를 애플리케이션의 User 모델과 동기화하거나 새로 생성
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserMapper userMapper;

    public CustomOAuth2UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    /**
     * OAuth2 사용자 정보를 로드하는 핵심 메서드.
     * @param userRequest 사용자 정보 요청 객체 (액세스 토큰 포함)
     * @return 커스텀 래핑된 OAuth2User 객체 (내부 User 모델 포함)
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 서비스의 loadUser를 호출하여 Spotify로부터 사용자 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 2. Spotify 사용자 속성 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String spotifyUserId = (String) attributes.get("id");
        // 3. DB에서 기존 사용자 검색
        User user = userMapper.findBySpotifyUserId(spotifyUserId);
        // 4. 사용자가 존재하지 않는 경우 (새로운 사용자)
        if (user == null) {
            // 새로운 사용자
            String username = (String) attributes.get("display_name");
            String email = (String) attributes.get("email");

            // username null이거나 빈칸일 때
            if (username == null || username.isBlank() || userMapper.findByUsername(username) != null) {
                username = "user_" + spotifyUserId;
            }

            // email null이거나 빈칸일 때
            if (email == null || email.isBlank() || userMapper.findByEmail(email) != null) {
                email = spotifyUserId + "@myplaylist.app";
            }
            // 애플리케이션의 User 모델 객체 생성 및 초기화
            user = new User();
            user.setSpotifyUserId(spotifyUserId);
            user.setUsername(username);
            user.setEmail(email);
        }
        // 5. OAuth2User와 애플리케이션의 User 모델을 래핑하여 반환
        return new CustomOAuth2User(oAuth2User, user);
    }
    /**
     * OAuth2User 인터페이스를 구현하며 애플리케이션의 User 모델을 포함하는 커스텀 클래스.
     */
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
