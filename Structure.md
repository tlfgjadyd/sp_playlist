# MyPlaylist 프로젝트 구조

이 문서는 MyPlaylist 애플리케이션의 주요 클래스와 메소드의 역할에 대해 설명합니다.

## 1. 설정 (`config` 패키지)

### `SecurityConfig.java`
- **역할**: Spring Security 관련 설정을 총괄합니다. 애플리케이션의 인증, 인가, OAuth2 로그인, 로그아웃 등 보안과 관련된 모든 흐름을 정의합니다.
- **주요 메소드**:
    - `securityFilterChain(HttpSecurity http, ...)`: HTTP 요청에 대한 보안 필터 체인을 구성합니다.
        - `.authorizeHttpRequests()`: URL 패턴별 접근 권한을 설정합니다. (예: 홈페이지는 모두 접근 가능, 그 외는 인증 필요)
        - `.oauth2Login()`: OAuth2 소셜 로그인 과정을 설정합니다.
            - `authorizationEndpoint()`: 로그인 요청을 커스터마이징하는 `CustomAuthorizationRequestResolver`를 등록합니다.
            - `userInfoEndpoint()`: 사용자 정보 처리 서비스로 `CustomOAuth2UserService`를 지정합니다.
            - `successHandler()`: 로그인 성공 후 로직을 처리할 `CustomAuthenticationSuccessHandler`를 지정합니다.

### `CustomAuthorizationRequestResolver.java`
- **역할**: Spring Security가 Spotify로 보내는 인증 요청(Authorization Request)을 중간에 가로채서 커스터마이징합니다.
- **주요 메소드**:
    - `resolve(...)`: 기본 리졸버가 생성한 요청을 가져옵니다.
    - `customizeAuthorizationRequest(...)`: 요청이 Spotify를 향하는 것인지 확인하고, `approval_prompt=force` 파라미터를 추가합니다. 이 파라미터는 Spotify가 항상 리프레시 토큰을 발급하도록 강제하여, 토큰 만료 후에도 자동 로그인이 가능하게 합니다.

### `CustomAuthenticationSuccessHandler.java`
- **역할**: 사용자가 성공적으로 Spotify 로그인을 마쳤을 때 호출되어, 후속 작업을 처리합니다.
- **주요 메소드**:
    - `onAuthenticationSuccess(...)`:
        1. 로그인한 사용자(`User`) 객체와 Spotify로부터 발급된 토큰(Access, Refresh)을 가져옵니다.
        2. `User` 객체에 토큰 정보와 만료 시간을 저장합니다.
        3. 사용자가 신규인지(`user.getId() == 0`), 기존 사용자인지 판단합니다.
        4. 신규 사용자면 `userMapper.insertUser()`를 호출하여 DB에 저장하고, 기존 사용자면 `userMapper.updateSpotifyTokens()`를 호출하여 토큰 정보를 업데이트합니다.

### `SpotifyApiConfig.java`
- **역할**: Spotify API 호출을 위한 핵심 객체인 `SpotifyApi`를 Spring의 Bean으로 등록합니다.
- **주요 메소드**:
    - `spotifyApi()`: `application.yml`에 설정된 `client-id`와 `client-secret`을 사용하여 `SpotifyApi` 객체를 생성하고, 애플리케이션 전역에서 주입하여 사용할 수 있도록 합니다.

## 2. 서비스 (`service` 패키지)

### `CustomOAuth2UserService.java`
- **역할**: Spotify로부터 사용자 정보를 성공적으로 받아온 직후 호출되어, 우리 시스템의 사용자(User)와 매핑하는 작업을 수행합니다.
- **주요 메소드**:
    - `loadUser(...)`:
        1. Spotify ID를 기준으로 `userMapper.findBySpotifyUserId()`를 통해 DB에 이미 가입된 사용자인지 확인합니다.
        2. **신규 사용자일 경우**: `username`과 `email`의 중복 또는 `null` 여부를 확인하여, 필요시 고유한 임시 값을 생성합니다. 이 정보로 임시 `User` 객체를 생성하여 다음 단계로 넘깁니다. (이 단계에서는 DB에 저장하지 않음)
        3. **기존 사용자일 경우**: DB에서 조회한 `User` 정보를 다음 단계로 넘깁니다.

### `SpotifyService.java`
- **역할**: Spotify API와의 모든 통신을 담당하는 서비스입니다.
- **주요 메소드**:
    - `getInitializedSpotifyApi(User user)`: API 호출 직전에 호출되어, 사용자의 Access Token이 만료되었는지 확인하고, 만료되었다면 Refresh Token을 사용해 새로운 Access Token을 발급받아 `SpotifyApi` 객체를 초기화합니다.
    - `getNewReleases(User user)`: Spotify의 최신 앨범 목록을 가져옵니다.
    - `getUsersTopTracks(User user)`: 현재 로그인한 사용자가 가장 많이 들은 트랙 목록을 가져옵니다.
    - `searchTracks(User user, String query)`: 주어진 검색어로 노래를 검색합니다.

## 3. 컨트롤러 (`controller` 패키지)

### `SpotifyController.java`
- **역할**: Spotify 데이터 조회와 관련된 웹 페이지 요청을 처리합니다. (예: 최신 앨범, Top 트랙, 검색)
- **주요 메소드**:
    - `@GetMapping`이 붙은 각 메소드들은 특정 URL 요청을 받아, `SpotifyService`를 통해 데이터를 조회하고, 해당 데이터를 `Model`에 담아 적절한 `html` 템플릿으로 렌더링합니다.

### `PlaylistController.java`
- **역할**: 사용자의 플레이리스트 생성, 조회, 수정 등 플레이리스트 관리와 관련된 모든 웹 페이지 요청을 처리합니다.
- **주요 메소드**:
    - `showMyPlaylists(...)`: 내 플레이리스트 목록 페이지를 보여줍니다.
    - `createPlaylist(...)`: 새로운 플레이리스트를 생성하고 DB에 저장합니다.
    - `showPlaylistDetail(...)`: 특정 플레이리스트의 상세 페이지(수록곡 목록)를 보여줍니다.
    - `addTrackToPlaylist(...)`: 특정 플레이리스트에 노래를 추가합니다.

## 4. 데이터 접근 (`mapper` 패키지 및 `resources/mapper/*.xml`)

- **역할**: MyBatis 프레임워크를 사용하여 데이터베이스(MySQL)와 통신합니다. `*Mapper.java` 인터페이스와 `*-mapper.xml` 파일이 한 쌍을 이룹니다.
- **주요 매퍼**:
    - `UserMapper`: `users` 테이블에 대한 CRUD(생성, 조회, 수정, 삭제) 작업을 정의합니다.
    - `PlaylistMapper`: `playlists` 테이블에 대한 작업을 정의합니다.
    - `PlaylistTrackMapper`: `playlist_tracks` 테이블(플레이리스트와 곡의 관계)에 대한 작업을 정의합니다.
