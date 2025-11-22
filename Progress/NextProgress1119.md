# MyPlaylist 프로젝트 향후 진행 계획 및 Q&A (11/19)

이 문서는 프로젝트 제출 및 향후 발전을 위해 필요한 주요 질문들과 해결 방안을 정리합니다.

---

### 1. Spotify API 로그인 사용자 제한 해결 방법

**문제 상황**: 현재 Spotify API가 **개발 모드(Development Mode)**로 설정되어 있어, Spotify 개발자 대시보드의 'Users and Access' 탭에 이메일을 등록한 사용자만 서비스에 로그인할 수 있습니다.

**공식 해결책**:
-   모든 Spotify 사용자가 로그인할 수 있도록 하려면, Spotify 측에 앱 검토를 요청하여 **공개 모드(Extended Quota Mode)**로 전환해야 합니다. 이것이 유일한 공식 방법입니다.
-   기술적으로 이 제한을 우회하는 방법은 존재하지 않으며, 이는 Spotify의 보안 및 개인정보 보호 정책의 일환입니다.

**현실적인 대안**:
-   **프로젝트 제출 및 데모용**: 현재 방식대로, 서비스를 테스트하거나 평가해야 할 사용자의 Spotify 계정 이메일을 미리 받아서 'Users and Access'에 수동으로 추가하는 것이 개발 모드에서 앱을 공유하는 표준 절차입니다.
-   **실제 서비스 준비**: 장기적으로 실제 서비스를 목표로 한다면, 시간을 투자하여 Spotify에 앱 공개 전환을 신청하는 것이 필수적입니다.

---

### 2. `ngrok`을 대체하는 로컬 개발 환경 구성

**문제 상황**: 현재 `ngrok`을 사용하여 외부 HTTPS 터널을 생성하고 이를 Redirect URI로 사용하고 있으나, 로컬 개발 환경에서는 더 간편한 방법이 필요합니다.

**해결책**: **Spotify는 로컬 개발용으로 `http://localhost` 주소를 예외적으로 허용합니다.**

`ngrok` 없이 개발 환경을 구성하는 방법은 다음과 같습니다.

1.  **Spotify 개발자 대시보드 접속**: [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)에서 해당 애플리케이션을 선택합니다.
2.  **앱 설정(Settings) 변경**: `Redirect URIs` 항목을 편집합니다.
3.  **`localhost` 주소 추가**: 아래의 주소를 `Redirect URIs` 목록에 추가하고 저장합니다.
    ```
    http://localhost:8080/login/oauth2/code/spotify
    ```
    *(만약 애플리케이션의 포트 번호나 콜백 경로가 다르다면 실제 환경에 맞게 수정해야 합니다.)*
4.  **로컬 서버 실행**: 이제 `ngrok` 없이 로컬에서 Spring Boot 서버를 실행하고, 웹 브라우저에서 `http://localhost:8080`으로 접속하여 모든 기능을 정상적으로 테스트할 수 있습니다.

---

### 3. 프로젝트 배포 과정 가이드 (초보자용)

배포 경험이 없는 사용자를 위해, PaaS(Platform as a Service) 클라우드 서비스인 **Render**를 이용한 단계별 배포 가이드를 제공합니다.

#### 1단계: 코드 수정 (배포 준비)

배포 시 민감한 정보(API 키, DB 접속 정보 등)를 코드에서 분리하기 위해 **환경 변수(Environment Variables)**를 사용하도록 코드를 수정합니다.

1.  **`application.properties` 수정**:
    민감한 정보들을 `${...}` 구문을 사용하여 환경 변수로부터 읽어오도록 변경합니다.

    ```properties
    # Spotify API Keys
    spotify.client-id=${SPOTIFY_CLIENT_ID}
    spotify.client-secret=${SPOTIFY_CLIENT_SECRET}

    # Database Connection
    spring.datasource.url=${SPRING_DATASOURCE_URL}
    spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
    spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

    # Spring Security OAuth2 Client
    spring.security.oauth2.client.registration.spotify.client-id=${SPOTIFY_CLIENT_ID}
    spring.security.oauth2.client.registration.spotify.client-secret=${SPOTIFY_CLIENT_SECRET}
    ```

#### 2단계: Render 서비스 생성 및 설정

1.  **Render 가입**: [Render.com](https://render.com/)에 GitHub 계정으로 가입합니다.

2.  **데이터베이스 생성**:
    -   Render 대시보드에서 **[New] > [PostgreSQL]**을 선택합니다.
    -   데이터베이스 이름(예: `myplaylist-db`)을 지정하고, 무료(Free) 플랜으로 생성합니다.
    -   생성 완료 후, 'Info' 탭에 있는 **Internal Database URL**을 복사합니다. (사용자 이름, 비밀번호, 주소 포함)

3.  **웹 서비스 생성**:
    -   **[New] > [Web Service]**를 선택하고, 배포할 프로젝트의 GitHub 저장소를 연결합니다.
    -   아래와 같이 서비스 설정을 구성합니다.
        -   **Name**: 서비스 이름 (예: `myplaylist-app`)
        -   **Runtime**: `Java`
        -   **Build Command**: `./gradlew bootJar`
        -   **Start Command**: `java -jar build/libs/myplaylist-0.0.1-SNAPSHOT.jar` (실제 생성되는 `.jar` 파일명과 일치해야 함)
        -   **Plan**: `Free`

4.  **환경 변수 설정**:
    -   생성한 웹 서비스의 **'Environment'** 탭으로 이동합니다.
    -   **[Add Environment Variable]** 버튼을 눌러 1단계에서 정의한 모든 환경 변수들을 키-값 형태로 입력합니다.
        -   `SPOTIFY_CLIENT_ID`: Spotify 앱의 Client ID
        -   `SPOTIFY_CLIENT_SECRET`: Spotify 앱의 Client Secret
        -   `SPRING_DATASOURCE_URL`: 2단계에서 복사한 데이터베이스 URL
        -   `SPRING_DATASOURCE_USERNAME`: 데이터베이스 사용자 이름
        -   `SPRING_DATASOURCE_PASSWORD`: 데이터베이스 비밀번호

#### 3단계: 최종 배포 및 확인

1.  **배포 실행**:
    -   **[Create Web Service]** 버튼을 클릭하면 Render가 자동으로 소스코드를 빌드하고 배포를 시작합니다.
    -   'Logs' 탭에서 실시간 배포 과정을 확인할 수 있습니다.

2.  **Spotify Redirect URI 업데이트**:
    -   배포가 성공적으로 완료되면, Render는 `https://myplaylist-app.onrender.com`과 같은 공개 URL을 제공합니다.
    -   이 URL을 **Spotify 개발자 대시보드**의 `Redirect URIs` 목록에 반드시 추가해야 합니다. (경로 포함)
        ```
        https://myplaylist-app.onrender.com/login/oauth2/code/spotify
        ```

3.  **최종 테스트**:
    -   부여된 공개 URL로 접속하여 회원가입, 로그인, 플레이리스트 생성 등 모든 기능이 정상적으로 동작하는지 확인합니다.
