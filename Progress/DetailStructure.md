# 프로젝트 구조 및 상세 설명

이 문서는 프로젝트의 패키지, 클래스, 메서드, 리소스 및 이들의 목적과 상호 관계를 포함한 프로젝트 구조에 대한 자세한 개요를 제공합니다.

## 1. Java 소스 코드

### `com.playlist.myplaylist.controller`

이 패키지에는 웹 요청을 처리하고 애플리케이션 흐름을 조정하는 Spring MVC 컨트롤러가 포함되어 있습니다.

*   **`SpotifyController.java`**
    *   **목적:** Spotify 기능과 관련된 HTTP 요청을 처리합니다. `SpotifyService`를 통해 Spotify API에서 데이터를 가져와 다양한 HTML 템플릿에 표시할 수 있도록 준비합니다.
    *   **주요 메서드:**
        *   `login()`: 로그인 페이지를 표시합니다.
        *   `getNewReleases()`: 새로운 앨범 릴리스를 가져와 표시합니다.
        *   `getTopTracks()`: 인증된 사용자의 인기 트랙을 가져와 표시합니다.
        *   `search()`: 트랙 검색 요청을 처리하고 검색 결과를 표시합니다.
        *   `searchApi()`: 트랙 검색을 위한 REST API 엔드포인트를 제공하고 JSON을 반환합니다.
        *   `getNewReleasesApi()`: 새로운 앨범 릴리스를 위한 REST API 엔드포인트를 제공하고 JSON을 반환합니다.
        *   `showAlbumDetails()`: 특정 앨범의 세부 정보를 해당 트랙을 포함하여 표시하며, 트랙 데이터를 UI용 `TrackViewModel`로 포맷합니다.
*   **`PlaylistController.java`**
    *   **목적:** 사용자 플레이리스트와 관련된 HTTP 요청을 처리합니다. 플레이리스트 생성, 보기, 트랙 추가, 플레이리스트 및 플레이리스트 내 트랙 삭제를 위한 엔드포인트를 제공합니다. 데이터베이스 작업을 위해 `PlaylistMapper` 및 `PlaylistTrackMapper`와 상호작용합니다.
    *   **주요 메서드:**
        *   `showCreatePlaylistForm()`: 새 플레이리스트 생성 양식을 표시합니다.
        *   `createPlaylist(String name, String description, CustomOAuth2UserService.CustomOAuth2User customOAuth2User)`: 인증된 사용자를 위한 새 플레이리스트를 생성하기 위해 양식 제출을 처리합니다.
        *   `showMyPlaylists(Model model, CustomOAuth2UserService.CustomOAuth2User customOAuth2User)`: 인증된 사용자가 소유한 모든 플레이리스트 목록을 표시합니다.
        *   `getMyPlaylistsApi(CustomOAuth2UserService.CustomOAuth2User customOAuth2User)`: 인증된 사용자의 플레이리스트를 JSON 형식으로 가져오기 위한 REST API 엔드포인트를 제공합니다.
        *   `showAddTrackToPlaylistForm(...)`: 기존 플레이리스트에 트랙을 추가하기 위한 양식을 표시하며, 트랙 세부 정보를 미리 채웁니다. *(참고: 이 메서드는 이제 주로 모달 방식으로 대체되었습니다.)*
        *   `addTrackToPlaylist(...)`: 지정된 플레이리스트에 트랙을 추가하는 요청을 처리합니다. 중복 트랙 확인 로직을 포함하며, 성공 또는 실패를 나타내는 JSON 응답을 반환합니다.
        *   `showPlaylistDetail(int playlistId, Model model)`: 특정 플레이리스트의 세부 정보와 포함된 모든 트랙을 표시합니다.
        *   `deleteTrackFromPlaylist(int playlistId, String trackId)`: 플레이리스트에서 특정 트랙을 삭제합니다.
        *   `deletePlaylist(int playlistId)`: 전체 플레이리스트와 연결된 모든 트랙을 삭제합니다.

### `com.playlist.myplaylist.config`

이 패키지에는 Spring Security 및 Spotify API 클라이언트에 대한 구성 클래스가 포함되어 있습니다.

*   **`CustomAuthenticationSuccessHandler.java`**
    *   **목적:** 성공적인 OAuth2 인증 이벤트를 가로채는 사용자 정의 Spring Security `AuthenticationSuccessHandler`입니다. Spotify 액세스 및 새로 고침 토큰을 추출하고, 데이터베이스의 사용자 정보(토큰 및 만료 포함)를 업데이트한 다음 사용자를 홈페이지로 리디렉션합니다.
    *   **주요 메서드:**
        *   `onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)`: 성공적인 인증 시 실행되는 핵심 메서드입니다.
*   **`CustomAuthorizationRequestResolver.java`**
    *   **목적:** OAuth2 인증 요청을 사용자 정의하며, 특히 Spotify에 대해 사용자 승인을 항상 요구하도록 `approval_prompt=force`를 인증 요청에 추가합니다.
    *   **주요 메서드:**
        *   `resolve(HttpServletRequest request)`: `OAuth2AuthorizationRequest`를 해결하고 사용자 정의 매개변수를 적용합니다.
        *   `resolve(HttpServletRequest request, String clientRegistrationId)`: `OAuth2AuthorizationRequest`를 해결하기 위한 오버로드된 메서드입니다.
        *   `customizeAuthorizationRequest(OAuth2AuthorizationRequest req)`: `approval_prompt=force` 매개변수를 추가하는 내부 도우미 메서드입니다.
*   **`SecurityConfig.java`**
    *   **목적:** 애플리케이션의 Spring Security를 구성하여 Spotify를 통한 OAuth2 로그인을 활성화합니다. 접근 규칙을 정의하고, OAuth2를 위한 사용자 정의 사용자 서비스를 설정하며, 사용자 정의 성공 핸들러를 지정하고, Spotify 로그인 동작을 수정하기 위해 사용자 정의 권한 부여 요청 리졸버를 통합합니다.
    *   **주요 메서드/빈:**
        *   `securityFilterChain(...)`: 보안 필터 체인을 정의하고 권한 부여, OAuth2 로그인 및 로그아웃을 구성합니다.
        *   `authorizedClientService(...)`: `InMemoryOAuth2AuthorizedClientService`를 제공합니다.
        *   `authorizedClientRepository(...)`: `AuthenticatedPrincipalOAuth2AuthorizedClientRepository`를 제공합니다.
        *   `authorizationRequestResolver(...)`: `CustomAuthorizationRequestResolver` 인스턴스를 생성하고 반환합니다.
*   **`SpotifyApiConfig.java`**
    *   **목적:** `SpotifyApi`의 싱글톤 인스턴스를 생성하고 제공하는 구성 클래스입니다. 애플리케이션 속성에서 Spotify API 클라이언트 ID와 클라이언트 시크릿을 가져옵니다.
    *   **주요 메서드/빈:**
        *   `spotifyApi()`: `SpotifyApi` 인스턴스를 구성하고 반환하는 `@Bean` 메서드입니다.

### `com.playlist.myplaylist.mapper`

이 패키지에는 데이터베이스와 상호작용하기 위한 계약을 정의하는 MyBatis Mapper 인터페이스가 포함되어 있습니다.

*   **`PlaylistMapper.java`**
    *   **목적:** `Playlist` 엔터티에 대한 CRUD 작업을 수행하기 위한 MyBatis Mapper 인터페이스입니다.
    *   **주요 메서드:**
        *   `insertPlaylist(Playlist playlist)`: 새 플레이리스트를 삽입합니다.
        *   `findByUserId(int userId)`: 사용자 ID로 플레이리스트를 검색합니다.
        *   `findById(int id)`: ID로 플레이리스트를 검색합니다.
        *   `deletePlaylistById(int id)`: ID로 플레이리스트를 삭제합니다.
*   **`PlaylistTrackMapper.java`**
    *   **목적:** `PlaylistTrack` 엔터티에 대한 CRUD 및 쿼리 작업을 수행하여 플레이리스트와 트랙 간의 관계를 관리하기 위한 MyBatis Mapper 인터페이스입니다.
    *   **주요 메서드:**
        *   `insertPlaylistTrack(PlaylistTrack playlistTrack)`: 새 트랙을 플레이리스트에 삽입합니다.
        *   `findByPlaylistId(int playlistId)`: 특정 플레이리스트의 모든 트랙을 검색합니다.
        *   `existsByPlaylistIdAndTrackId(...)`: 트랙이 플레이리스트에 존재하는지 확인합니다.
        *   `deleteByPlaylistIdAndTrackId(...)`: 플레이리스트에서 트랙을 삭제합니다.
*   **`UserMapper.java`**
    *   **목적:** `User` 엔터티에 대한 CRUD 작업을 수행하여 사용자 세부 정보 및 Spotify 토큰 관리를 처리하기 위한 MyBatis Mapper 인터페이스입니다.
    *   **주요 메서드:**
        *   `findBySpotifyUserId(String spotifyUserId)`: Spotify 사용자 ID로 사용자를 검색합니다.
        *   `findByUsername(String username)`: 사용자 이름으로 사용자를 검색합니다.
        *   `findByEmail(String email)`: 이메일로 사용자를 검색합니다.
        *   `insertUser(User user)`: 새 사용자를 삽입합니다.
        *   `updateSpotifyTokens(User user)`: Spotify 액세스 및 새로 고침 토큰을 업데이트합니다.
        *   `updateSpotifyAccessToken(User user)`: Spotify 액세스 토큰만 업데이트합니다.

### `com.playlist.myplaylist.model`

이 패키지는 애플리케이션의 데이터 모델(POJO) 및 DTO를 정의합니다.

*   **`TrackViewModel.java`**
    *   **목적:** UI용 트랙의 간소화된 보기를 나타내는 데이터 전송 객체(DTO)입니다.
    *   **주요 필드:** `id`, `name`, `artists`, `duration`, `trackNumber`.
*   **`Playlist.java`**
    *   **목적:** 애플리케이션의 도메인 모델에서 플레이리스트 엔터티를 나타냅니다.
    *   **주요 필드:** `id`, `userId`, `name`, `description`, `createdAt`.
*   **`PlaylistTrack.java`**
    *   **목적:** 플레이리스트 내의 트랙을 나타내며, 플레이리스트와 특정 Spotify 트랙 간의 관계를 설정합니다.
    *   **주요 필드:** `id`, `playlistId`, `trackId`, `trackName`, `artistName`, `imageUrl`, `createdAt`.
*   **`User.java`**
    *   **목적:** 사용자 엔터티를 나타내며, 내부 ID, 사용자 이름, 이메일, Spotify 사용자 ID, 생성 타임스탬프 및 Spotify OAuth2 토큰을 저장합니다.
    *   **주요 필드:** `id`, `username`, `email`, `spotifyUserId`, `createdAt`, `spotifyAccessToken`, `spotifyRefreshToken`, `spotifyAccessTokenExpiresAt`.

### `com.playlist.myplaylist.service`

이 패키지에는 비즈니스 로직을 캡슐화하고 외부 API 또는 매퍼와 상호작용하는 서비스 클래스가 포함되어 있습니다.

*   **`SpotifyService.java`**
    *   **목적:** Spotify 웹 API와 상호작용하기 위한 인터페이스를 제공합니다. Spotify API 인증(토큰 갱신) 및 다양한 데이터 검색 작업을 처리합니다.
    *   **주요 메서드:**
        *   `getInitializedSpotifyApi(User user)`: Spotify API 초기화 및 토큰 갱신을 위한 내부 도우미입니다.
        *   `getNewReleases(User user, int limit, int offset)`: 새로운 앨범 릴리스를 가져옵니다.
        *   `getUsersTopTracks(User user)`: 인증된 사용자의 인기 트랙을 가져옵니다.
        *   `searchTracks(User user, String query, int offset)`: 트랙을 검색합니다.
        *   `getAlbum(User user, String albumId)`: 앨범 세부 정보를 검색합니다.
        *   `getAlbumTracks(User user, String albumId)`: 앨범의 트랙을 검색합니다.
*   **`CustomOAuth2UserService.java`**
    *   **목적:** Spotify에 대한 OAuth2 사용자 정보 로딩을 사용자 정의하고, 사용자 세부 정보 검색, 사용자 존재 여부 확인, 새 사용자 생성 및 `OAuth2User`를 사용자 정의 `CustomOAuth2User` 객체로 래핑하는 작업을 처리합니다.
    *   **주요 메서드:**
        *   `loadUser(OAuth2UserRequest userRequest)`: OAuth2 사용자 정보를 처리하기 위한 기본 동작을 재정의합니다.
    *   **내부 클래스 `CustomOAuth2User`:** 애플리케이션의 `User` 모델을 포함하는 사용자 정의 `OAuth2User` 구현입니다.

### `com.playlist.myplaylist.MyplaylistApplication.java`

*   **목적:** Spring Boot 애플리케이션의 주요 진입점이며, 애플리케이션 컨텍스트를 초기화하고 실행하는 역할을 합니다.
*   **어노테이션:** `@SpringBootApplication`, `@MapperScan`.
*   **주요 메서드:** `main(String[] args)`: Spring Boot 애플리케이션을 시작합니다.

## 2. MyBatis XML 매퍼

이 XML 파일은 MyBatis 매퍼 인터페이스에서 사용하는 SQL 문을 정의합니다.

*   **`playlist-mapper.xml`**
    *   **경로:** `src/main/resources/mapper/playlist-mapper.xml`
    *   **목적:** `PlaylistMapper.java`에 해당하는 `playlists` 테이블에 대한 CRUD 작업용 SQL을 정의합니다.
    *   **주요 매핑:** `insertPlaylist`, `findByUserId`, `findById`, `deletePlaylistById`.
*   **`playlist-track-mapper.xml`**
    *   **경로:** `src/main/resources/mapper/playlist-track-mapper.xml`
    *   **목적:** `PlaylistTrackMapper.java`에 해당하는 `playlist_tracks` 테이블에서 플레이리스트 내 트랙 관리를 위한 SQL을 정의합니다.
    *   **주요 매핑:** `insertPlaylistTrack`, `findByPlaylistId`, `existsByPlaylistIdAndTrackId`, `deleteByPlaylistIdAndTrackId`.
*   **`user-mapper.xml`**
    *   **경로:** `src/main/resources/mapper/user-mapper.xml`
    *   **목적:** `UserMapper.java`에 해당하는 `users` 테이블에서 사용자 데이터 및 Spotify OAuth2 토큰 관리를 위한 SQL을 정의합니다.
    *   **주요 매핑:** `findBySpotifyUserId`, `findByUsername`, `findByEmail`, `insertUser`, `updateSpotifyTokens`, `updateSpotifyAccessToken`.
*   **`mybatis-config.xml`**
    *   **경로:** `src/main/resources/mybatis-config.xml`
    *   **목적:** MyBatis의 주요 구성 파일로, 현재는 향후 구성을 위한 최소한의 플레이스홀더입니다.

## 3. HTML 템플릿

이것들은 웹 애플리케이션의 뷰 렌더링에 사용되는 Thymeleaf HTML 템플릿입니다.

*   **`index.html`**
    *   **경로:** `src/main/resources/templates/index.html`
    *   **목적:** 애플리케이션의 주요 랜딩 페이지로, 환영 메시지와 사용자 인증에 따른 조건부 콘텐츠를 제공합니다.
    *   **주요 기능:** 인증된/익명 사용자를 위한 조건부 표시, 다른 섹션으로의 링크.
*   **`login.html`**
    *   **경로:** `src/main/resources/templates/login.html`
    *   **목적:** 사용자가 OAuth2를 사용하여 Spotify 계정을 통해 로그인할 수 있는 전용 페이지입니다.
    *   **주요 기능:** Spotify 로그인 버튼, Bootstrap 스타일링.
*   **`playlist-create.html`**
    *   **경로:** `src/main/resources/templates/playlist-create.html`
    *   **목적:** 사용자가 새 플레이리스트를 생성할 수 있는 양식을 제공합니다.
    *   **주요 기능:** 이름 및 설명을 위한 양식, Bootstrap 스타일링.
*   **`playlist-detail.html`**
    *   **경로:** `src/main/resources/templates/playlist-detail.html`
    *   **목적:** 특정 플레이리스트의 상세 보기를 표시하며, 이름, 설명 및 트랙을 포함합니다.
    *   **주요 기능:** 트랙 목록 테이블, 트랙 삭제 기능.
*   **`top-tracks.html`**
    *   **경로:** `src/main/resources/templates/top-tracks.html`
    *   **목적:** 인증된 사용자의 Spotify 인기 트랙 목록을 표시합니다.
    *   **주요 기능:** 트랙 목록, "플레이리스트에 추가" 모달, 토스트 알림.
*   **`search-results.html`**
    *   **경로:** `src/main/resources/templates/search-results.html`
    *   **목적:** 트랙 검색 결과를 표시하고, 모달을 통해 플레이리스트에 트랙을 추가할 수 있으며, 무한 스크롤을 구현합니다.
    *   **주요 기능:** 검색 양식, 무한 스크롤이 있는 트랙 목록, "플레이리스트에 추가" 모달, 토스트 알림.
*   **`album-detail.html`**
    *   **경로:** `src/main/resources/templates/album-detail.html`
    *   **목적:** Spotify 앨범 및 트랙에 대한 자세한 정보를 표시합니다.
    *   **주요 기능:** 앨범 메타데이터, 트랙 목록, "플레이리스트에 추가" 모달.
*   **`playlist-my.html`**
    *   **경로:** `src/main/resources/templates/playlist-my.html`
    *   **목적:** 인증된 사용자가 생성한 모든 플레이리스트 목록을 표시합니다.
    *   **주요 기능:** 플레이리스트 생성 버튼, 삭제 기능이 있는 플레이리스트 목록.
*   **`new-releases.html`**
    *   **경로:** `src/main/resources/templates/new-releases.html`
    *   **목적:** 무한 스크롤이 있는 Spotify의 새 앨범 릴리스 페이지 목록을 표시합니다.
    *   **주요 기능:** 무한 스크롤이 있는 앨범 목록, 앨범 세부 정보 링크.
*   **`global-top.html`**
    *   **경로:** `src/main/resources/templates/global-top.html`
    *   **목적:** 전역 탐색, 공통 `<head>` 콘텐츠 및 토스트 알림 시스템과 같은 재사용 가능한 HTML 조각을 포함합니다.
    *   **주요 조각:** `common-head`, `global-top`, `toast-container`, `toast-script`.
*   **`playlist-add-track.html`**
    *   **경로:** `src/main/resources/templates/playlist-add-track.html`
    *   **목적:** 사용자가 특정 트랙을 기존 플레이리스트에 추가할 수 있도록 합니다(다른 페이지의 모달 방식으로 대체됨).
    *   **주요 기능:** 트랙 세부 정보, 플레이리스트 선택 드롭다운, AJAX 제출.

## 4. 정적 리소스 (CSS)

이 파일은 웹 애플리케이션의 스타일링을 제공합니다.

*   **`custom.css`**
    *   **경로:** `src/main/resources/static/css/custom.css`
    *   **목적:** Bootstrap의 기본 스타일을 재정의하거나 확장하여 시각적 모양과 사용자 경험을 향상시키기 위한 사용자 정의 스타일링을 제공합니다.
    *   **주요 스타일:** 전역 본문 스타일, 카드/목록 항목에 대한 호버 효과, 사용자 정의 기본 버튼 색상, 탐색 브랜드 스타일링.