package com.playlist.myplaylist.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security OAuth2 권한 부여 요청을 사용자 정의하는 리졸버.
 * 특히, Spotify 로그인 시 'approval_prompt=force' 파라미터를 추가하여
 * 사용자에게 항상 승인 화면을 표시하도록 강제하는 로직을 포함
 */
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    // 기본 OAuth2AuthorizationRequestResolver 인스턴스 (Spring Security의 기본 동작 수행)
    private final OAuth2AuthorizationRequestResolver defaultResolver;
    /**
     * 생성자: 기본 리졸버를 초기화
     * @param repo 클라이언트 등록 정보 저장소 (ClientRegistrationRepository)
     * @param authorizationRequestBaseUri 권한 부여 요청의 기본 URI (예: /oauth2/authorization)
     */
    public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo, String authorizationRequestBaseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, authorizationRequestBaseUri);
    }
    /**
     * HttpServletRequest를 사용하여 권한 부여 요청을 해결하고 사용자 정의를 적용
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        // 기본 리졸버를 사용하여 요청을 처리
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
        // 사용자 정의 로직을 적용합니다.
        return customizeAuthorizationRequest(req);
    }
    /**
     * HttpServletRequest와 클라이언트 ID를 사용하여 권한 부여 요청을 해결하고 사용자 정의를 적용
     */
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(req);
    }
    /**
     * 권한 부여 요청 객체에 'approval_prompt=force' 파라미터를 추가하는 내부 도우미 메서드.
     * @param req 기본 리졸버가 생성한 OAuth2AuthorizationRequest 객체
     * @return 사용자 정의 파라미터가 추가된 OAuth2AuthorizationRequest 객체
     */
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest req) {
        if (req == null) {
            return null;
        }

        // 특정 조건 확인: Spotify 클라이언트 등록 ID에 해당하는지 확인하는 것으로 추정되는 조건
        if (!req.getAuthorizationUri().contains("spotify.com")) {
            return req;
        }

        Map<String, Object> additionalParameters = new HashMap<>(req.getAdditionalParameters());
        // Spotify API에 대한 승인 프롬프트를 강제하는 파라미터를 추가
        // 이는 사용자가 이미 승인했더라도 다시 권한 요청 화면을 출력
        additionalParameters.put("approval_prompt", "force");
        // 기존 요청 객체를 기반으로 새로운 요청 객체를 빌드
        return OAuth2AuthorizationRequest.from(req)
                .additionalParameters(additionalParameters)
                .build();
    }
}
