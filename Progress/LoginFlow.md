# MyPlaylist 로그인 및 신규 등록 흐름 분석

이 문서는 사용자가 "Login with Spotify" 버튼을 클릭한 후 애플리케이션에 로그인되기까지의 전체 과정을 설명합니다.

## 1. 전체 흐름 개요

1.  **사용자 시작**: 사용자가 로그인 버튼을 클릭하여 Spotify 인증을 시작합니다.
2.  **Spring Security OAuth2**: Spring Security가 Spotify와의 OAuth2 인증 과정을 처리합니다. 사용자는 Spotify에 로그인하고 데이터 접근 권한을 부여합니다.
3.  **콜백 및 사용자 정보 로드**: Spotify가 인증 코드를 포함하여 우리 애플리케이션으로 리디렉션합니다. Spring Security는 이 코드를 사용하여 Access Token을 얻고, 이어서 `CustomOAuth2UserService`를 호출하여 Spotify로부터 사용자 정보를 가져옵니다.
4.  **사용자 조회 또는 생성**: `CustomOAuth2UserService`의 `loadUser` 메소드는 DB에서 해당 Spotify 사용자가 이미 있는지 확인합니다.
    *   **기존 사용자**: DB에서 사용자 정보를 로드합니다.
    *   **신규 사용자**: DB에 아직 저장되지 않은 임시 `User` 객체를 생성합니다. (이때 username/email 중복 및 null 값 처리)
5.  **로그인 성공 처리**: `CustomAuthenticationSuccessHandler`가 호출됩니다.
    *   Spotify로부터 받은 Access Token, Refresh Token, 만료 시간을 `User` 객체에 저장합니다.
    *   `loadUser`에서 넘어온 `User` 객체의 ID 값을 확인하여 신규/기존 사용자를 구분합니다.
    *   **신규 사용자 (`id == 0`)**: 모든 정보가 채워진 `User` 객체를 DB에 `INSERT`합니다.
    *   **기존 사용자 (`id != 0`)**: 새로 받은 토큰 정보로 DB를 `UPDATE`합니다.
6.  **리디렉션**: 사용자는 애플리케이션의 메인 페이지(`/new-releases`)로 리디렉션됩니다.

---

## 2. 핵심 클래스 및 메소드 상세

### `SecurityConfig.java`

-   Spring Security의 설정을 담당합니다.
-   `.oauth2Login()` 체인을 통해 로그인 과정을 설정합니다.
-   **`loginPage("/login")`**: 로그인 페이지 경로를 지정합니다.
-   **`userInfoEndpoint().userService(customOAuth2UserService)`**: Spotify로부터 사용자 정보를 성공적으로 가져왔을 때, 후처리를 담당할 서비스로 `CustomOAuth2UserService`를 지정합니다.
-   **`successHandler(customAuthenticationSuccessHandler)`**: 모든 인증 과정이 성공적으로 끝났을 때 최종 처리를 담당할 핸들러로 `CustomAuthenticationSuccessHandler`를 지정합니다.

### `CustomOAuth2UserService.java`

-   **`loadUser(OAuth2UserRequest userRequest)` 메소드**:
    1.  Spring Security의 `DefaultOAuth2UserService`를 통해 기본적인 OAuth2 사용자 정보(`OAuth2User`)를 가져옵니다.
    2.  `oAuth2User.getAttributes()`를 통해 Spotify 사용자 ID, 표시 이름, 이메일 등의 속성을 얻습니다.
    3.  `userMapper.findBySpotifyUserId()`를 호출하여 이 사용자가 우리 DB에 이미 있는지 확인합니다.
    4.  **`if (user == null)` (신규 사용자일 경우)**:
        a.  Spotify가 제공한 `display_name`과 `email`을 가져옵니다.
        b.  **Username 처리**: `display_name`이 `null`이거나, 비어있거나, DB에 이미 존재하는(`userMapper.findByUsername()`) 경우, `user_[spotify_user_id]` 형태로 고유한 이름을 생성합니다.
        c.  **Email 처리**: `email`이 `null`이거나, 비어있거나, DB에 이미 존재하는(`userMapper.findByEmail()`) 경우, `[spotify_user_id]@myplaylist.app` 형태로 고유한 임시 이메일을 생성합니다.
        d.  이 정보들을 바탕으로 DB에 저장되지 않은 임시 `User` 객체를 생성합니다. 이 객체의 `id`는 `int`의 기본값인 `0`입니다.
    5.  조회했거나 새로 생성한 `User` 객체를 포함하는 `CustomOAuth2User`를 생성하여 반환합니다.

### `CustomAuthenticationSuccessHandler.java`

-   **`onAuthenticationSuccess(...)` 메소드**:
    1.  `loadUser`에서 반환된 `CustomOAuth2User`로부터 `User` 객체를 가져옵니다.
    2.  `OAuth2AuthorizedClientService`를 통해 Spotify로부터 발급받은 Access Token, Refresh Token, 만료 시간을 가져옵니다.
    3.  가져온 토큰 정보들을 `User` 객체에 설정(`set`)합니다.
    4.  **`if (user.getId() == 0)` (신규 사용자일 경우)**:
        -   `userMapper.insertUser(user)`를 호출하여 모든 정보(사용자 정보 + 토큰 정보)가 채워진 `User` 객체를 DB에 삽입합니다.
    5.  **`else` (기존 사용자일 경우)**:
        -   `userMapper.updateSpotifyTokens(user)`를 호출하여 DB에 있는 해당 사용자의 토큰 정보를 갱신합니다.
    6.  사용자를 `/new-releases` 페이지로 리디렉션합니다.

---

## 3. 사용된 SQL 쿼리 (`user-mapper.xml`)

### 사용자 조회

-   **`findBySpotifyUserId`**: `loadUser`에서 사용자를 처음 조회할 때 사용됩니다.
    ```xml
    <select id="findBySpotifyUserId" resultMap="userResultMap">
        SELECT * FROM users WHERE spotify_user_id = #{spotifyUserId}
    </select>
    ```
-   **`findByUsername`**: 신규 사용자 등록 시 이름 중복을 확인하기 위해 사용됩니다.
    ```xml
    <select id="findByUsername" resultMap="userResultMap">
        SELECT * FROM users WHERE username = #{username}
    </select>
    ```
-   **`findByEmail`**: 신규 사용자 등록 시 이메일 중복을 확인하기 위해 사용됩니다.
    ```xml
    <select id="findByEmail" resultMap="userResultMap">
        SELECT * FROM users WHERE email = #{email}
    </select>
    ```

### 사용자 데이터 변경

-   **`insertUser`**: `onAuthenticationSuccess`에서 신규 사용자를 DB에 추가할 때 사용됩니다.
    ```xml
    <insert id="insertUser" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO users (username, email, spotify_user_id, spotify_access_token, spotify_refresh_token, spotify_access_token_expires_at)
        VALUES (#{username}, #{email}, #{spotifyUserId}, #{spotifyAccessToken}, #{spotifyRefreshToken}, #{spotifyAccessTokenExpiresAt})
    </insert>
    ```
-   **`updateSpotifyTokens`**: `onAuthenticationSuccess`에서 기존 사용자의 토큰 정보를 갱신할 때 사용됩니다.
    ```xml
    <update id="updateSpotifyTokens">
        UPDATE users
        SET
            spotify_access_token = #{spotifyAccessToken},
            spotify_refresh_token = #{spotifyRefreshToken},
            spotify_access_token_expires_at = #{spotifyAccessTokenExpiresAt}
        WHERE id = #{id}
    </update>
    ```
