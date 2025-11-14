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

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserMapper userMapper;

    public CustomAuthenticationSuccessHandler(OAuth2AuthorizedClientService authorizedClientService, UserMapper userMapper) {
        this.authorizedClientService = authorizedClientService;
        this.userMapper = userMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        CustomOAuth2UserService.CustomOAuth2User customUser = (CustomOAuth2UserService.CustomOAuth2User) oauthToken.getPrincipal();
        User user = customUser.getUser();

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;

        user.setSpotifyAccessToken(accessToken);
        if (refreshToken != null) {
            user.setSpotifyRefreshToken(refreshToken);
        }
        userMapper.updateSpotifyTokens(user);

        response.sendRedirect("/new-releases");
    }


}
