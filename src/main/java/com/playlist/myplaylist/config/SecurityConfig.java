package com.playlist.myplaylist.config;

import com.playlist.myplaylist.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spotify를 통한 OAuth2 로그인을 활성화하고 애플리케이션의 보안 규칙을 정의하는 설정 클래스.
 * &#064;EnableWebSecurity:  Spring Security 활성화를 명시
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    // 순환 참조 방지를 위해 @Lazy를 사용 (CustomAuthenticationSuccessHandler -> UserMapper -> Transactional Bean -> SecurityConfig)
    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService, @Lazy CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
    }
    // HTTP 요청에 대한 보안 필터 체인을 구성
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        http
                // 1. HTTP 요청에 대한 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 루트 페이지, 로그인, 에러 페이지, 정적 리소스(webjars)는 인증 없이 접근 허용
                        .requestMatchers("/", "/login", "/error", "/webjars/**").permitAll()
                        // 그 외 모든 요청은 인증(로그인)이 필요함
                        .anyRequest().authenticated()
                )
                // 2. OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // 커스텀 로그인 페이지 지정
                        .loginPage("/login")
                        // 권한 부여 엔드포인트 설정 (인증 요청 시)
                        .authorizationEndpoint(auth -> auth
                                // 사용자 정의 AuthorizationRequestResolver를 사용하여 요청 파라미터(CustomAuthorizationRequestResolver 참고: approval_prompt=force)를 추가
                                .authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository))
                        )
                        // 사용자 정보 엔드포인트 설정 (토큰 받은 후 사용자 정보 가져올 때)
                        .userInfoEndpoint(userInfo -> userInfo
                                // 사용자 정보 로딩을 위한 커스텀 서비스 지정
                                .userService(customOAuth2UserService)
                        )
                        // 인증 성공 후 실행될 커스텀 핸들러 지정 (토큰 DB 저장 로직 포함)
                        .successHandler(customAuthenticationSuccessHandler)
                )
                // 3. 로그아웃 설정
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * OAuth2 Authorized Client Service Bean 정의.
     * 인증된 클라이언트(액세스/리프레시 토큰) 정보를 메모리에 저장
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
    /**
     * OAuth2 Authorized Client Repository Bean 정의.
     * 인증된 주체(Principal)와 Authorized Client 객체를 연결하여 저장/로딩하는 방법을 정의.
     */
    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }
    /**
     * CustomAuthorizationRequestResolver 인스턴스를 생성하고 반환하는 헬퍼 메서드.
     * Spotify 권한 부여 요청에 커스텀 파라미터를 추가하는 역할을 수행
     * @param clientRegistrationRepository 클라이언트 등록 정보
     * @return 커스텀 리졸버
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        // 커스텀 리졸버에 ClientRegistrationRepository와 기본 URI를 전달하여 초기화
        return new CustomAuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }
}
