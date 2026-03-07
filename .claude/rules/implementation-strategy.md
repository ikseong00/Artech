# 구현 전략

## Phase 0: Foundation Setup

### #1: develop 브랜치 생성

- `main`에서 `develop` 브랜치 생성 및 push
- 브랜치 보호 규칙 설정 (선택)

### #2: 핵심 의존성 추가 + Desktop/Web 타겟 정리

- **의존성 추가**: Supabase-kt, Koin, Navigation Compose, kotlinx-serialization, kotlinx-datetime, Room KMP, Ktor 엔진 (Android: OkHttp, iOS: Darwin)
- **타겟 정리**: Desktop(JVM) 타겟 제거, Web(wasmJs) 타겟 제거
- `libs.versions.toml` 및 `build.gradle.kts` 수정
- Depends on: #1

### #3: Supabase 클라이언트 초기화 및 Koin DI 설정

- `SupabaseProvider` 구현 (Postgrest 플러그인)
- Koin `dataModule`, `viewModelModule` 설정
- 플랫폼별 `platformModule` (expect/actual)
- `Application` 클래스에서 Koin 초기화
- Depends on: #2

---

## Phase 1: Data Layer

### #4: Article 도메인 모델 및 DTO 정의

- `ArticleCategory` enum (13종, DB enum과 1:1 매핑)
- `ArticleDto` (kotlinx-serialization, Supabase 응답 매핑)
- `Article` 도메인 모델 (UI에서 사용)
- DTO → Domain 매핑 함수
- Depends on: #3

### #5: ArticleRepository 구현

- 페이지네이션 조회 (offset + limit)
- 카테고리별 필터링
- 키워드 검색 (title, summary ilike)
- `published_at` null 시 `created_at` fallback 정렬
- Depends on: #4

---

## Phase 2: Navigation + Home Screen

### #6: 앱 테마 및 디자인 시스템

- Material3 테마 (Light/Dark)
- Color, Typography 정의
- Depends on: #3

### #7: Bottom Tab Navigation 구조

- Route sealed interface (Home, Favorites, History, Settings)
- NavHost + BottomNavigationBar
- 4탭: 홈 / 즐겨찾기 / 읽기이력 / 설정
- Depends on: #6

### #8: ArticleCard 공통 컴포넌트

- 아티클 카드 UI (제목, 출처, 카테고리 뱃지, 시간)
- 클릭 이벤트 콜백
- Depends on: #6

### #9: HomeScreen 구현

- 아티클 리스트 (LazyColumn)
- 카테고리 필터 (가로 스크롤 ChipRow)
- 검색바
- 무한 스크롤 (페이지네이션)
- Pull to Refresh
- Loading / Empty / Error 상태 처리
- Depends on: #5, #7, #8

---

## Phase 3: Detail Screen

### #10: 플랫폼별 WebView expect/actual

- `expect` WebView Composable 정의
- Android: `AndroidView` + `android.webkit.WebView`
- iOS: `UIKitView` + `WKWebView`
- Depends on: #7

### #11: DetailScreen 구현

- WebView로 원문 표시
- 즐겨찾기 토글 버튼 (FAB 또는 TopBar action)
- 외부 브라우저로 열기
- 공유 기능
- Depends on: #10

---

## Phase 4: Favorites & History (Room KMP)

### #12: Room KMP 로컬 DB 설정

- `AppDatabase` 정의 (Room)
- `FavoriteEntity`, `ReadHistoryEntity`
- `FavoriteDao`, `ReadHistoryDao`
- 플랫폼별 `databaseBuilder` (expect/actual)
- Depends on: #3

### #13: FavoriteRepository + FavoriteScreen

- `FavoriteRepository` (Room CRUD)
- `FavoriteScreen` (즐겨찾기 목록, 삭제)
- `FavoriteViewModel`
- Depends on: #12, #8

### #14: HistoryRepository + HistoryScreen

- `HistoryRepository` (Room CRUD)
- `HistoryScreen` (날짜별 그룹핑, 전체 삭제)
- `HistoryViewModel`
- 아티클 열람 시 자동 기록
- Depends on: #12, #8

---

## Phase 5: Settings

### #15: 다크모드 및 앱 설정 저장

- `SettingsRepository` (DataStore 또는 Settings KMP)
- 다크모드 설정 (System/Light/Dark)
- Depends on: #3

### #16: SettingsScreen

- 다크모드 토글
- 읽기이력 전체 삭제
- 즐겨찾기 전체 삭제
- 앱 버전 표시
- Depends on: #15, #7

---

## Phase 6: Polish

### #17: 정렬 기능 + 수집 방식 탭

- 최신순 / 인기순 정렬 옵션
- System / User 수집 방식 탭 (추후 사용자 추가 아티클 분리)
- Depends on: #9

### #18: 에러 핸들링 통합 및 UI 폴리싱

- 네트워크 에러 Snackbar
- Retry 로직
- 로딩 스켈레톤
- 애니메이션 전환
- Depends on: #9, #11, #13, #14, #16

### #34: 전체 화면 리디자인 (완료)

- HistoryScreen 날짜별 그룹핑 (오늘/어제/이번 주/이전)
- SettingsScreen 아이콘 추가 (Material Icons) + 저작권 표시
- ArticleCard에 summary 텍스트 1줄 표시
- FavoriteScreen, HistoryScreen Empty State 개선 (아이콘 + 설명 + CTA 버튼)
- EmptyState 공통 컴포넌트 추출
- HistoryArticle 모델 추가 (readAt 정보 보존)
- Depends on: #13, #14, #16

---

## 의존성 다이어그램 (요약)

```
#1 → #2 → #3 → #4 → #5 ─────────────────────→ #9 → #17
                 │                                 ↑
                 ├→ #6 → #7 → #10 → #11          │    #18
                 │    │                            │     ↑
                 │    └→ #8 ───────────────────→ #9  #11,#13,#14,#16
                 │
                 ├→ #12 → #13 (+ #8)
                 │    └──→ #14 (+ #8)
                 │
                 └→ #15 → #16 (+ #7)
```
