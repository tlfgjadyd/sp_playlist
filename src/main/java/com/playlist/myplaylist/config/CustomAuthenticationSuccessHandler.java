package com.playlist.myplaylist.config;

import com.playlist.myplaylist.mapper.UserMapper;
import com.playlist.myplaylist.model.User;
import com.playlist.myplaylist.service.CustomOAuth2UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * OAuth2 인증(로그인) 성공 후 실행되는 커스텀 핸들러.
 * Spotify로부터 받은 액세스 및 리프레시 토큰을 추출하여 데이터베이스의 사용자 정보에 저장
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    // Spring Security의 OAuth2 인증 클라이언트 정보를 관리하는 서비스
    private final OAuth2AuthorizedClientService authorizedClientService;

    // 사용자 정보를 데이터베이스에 저장 & 업데이트하는 매퍼
    private final UserMapper userMapper;

    /**
     * 의존성 주입을 위한 생성자.
     * @param authorizedClientService OAuth2AuthorizedClientService
     * @param userMapper UserMapper
     */
    public CustomAuthenticationSuccessHandler(OAuth2AuthorizedClientService authorizedClientService, UserMapper userMapper) {
        this.authorizedClientService = authorizedClientService;
        this.userMapper = userMapper;
    }

    /**
     * 인증 성공 시 호출
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. 인증 객체에서 토큰 및 사용자 정보 추출
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        // Principal(인증 주체)에서 커스텀 사용자 객체 (User 모델 포함)를 가져옴
        CustomOAuth2UserService.CustomOAuth2User customUser = (CustomOAuth2UserService.CustomOAuth2User) oauthToken.getPrincipal();
        User user = customUser.getUser();// User 모델

        // 2. OAuth2AuthorizedClientService를 통해 토큰 정보를 로드
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName() // 사용자 이름
        );
        // 3. 액세스 토큰 및 만료 시간 추출
        String accessToken = client.getAccessToken().getTokenValue();
        Instant expiresAt = client.getAccessToken().getExpiresAt();
        // Instant를 애플리케이션에서 사용할 LocalDateTime으로 변환
        LocalDateTime expirationDateTime = LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault());
        // 4. 리프레시 토큰 추출
        // 만약 이번 인증 응답에 리프레시 토큰이 포함되어 있지 않다면 (Spotify는 보통 첫 로그인 시에만 제공)
        // 기존에 저장된 리프레시 토큰을 유지
        String refreshToken = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : user.getSpotifyRefreshToken();

        // 5. User 모델에 토큰 정보 업데이트
        user.setSpotifyAccessToken(accessToken);
        user.setSpotifyAccessTokenExpiresAt(expirationDateTime);

        if (refreshToken != null) {
            user.setSpotifyRefreshToken(refreshToken);
        }

        // 6. 데이터베이스에 사용자 정보 저장/업데이트
        // User ID가 0인 경우 (새로 생성된 사용자)는 삽입(insert)
        if (user.getId() == 0) {
            userMapper.insertUser(user);
        } else {
            // 기존 사용자인 경우 토큰 정보 업데이트 (update)
            userMapper.updateSpotifyTokens(user);
        }
        // 7. 메인 페이지로 리다이렉트하여 로그인 프로세스 완료
        response.sendRedirect("/");
    }


}
