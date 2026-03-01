# Artech 프로젝트 명세서

## 1. 프로젝트 개요

### 1.1 프로젝트 소개

Artech는 국내외 주요 기술 블로그 아티클을 한 곳에서 탐색할 수 있는 애그리게이터 앱입니다.
Supabase에 수집된 아티클을 카테고리별/키워드별로 조회하고, 즐겨찾기와 읽기이력을 로컬에 저장합니다.

### 1.2 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| **클라이언트** | Compose Multiplatform (Android, iOS) | 1.10.0 |
| **언어** | Kotlin | 2.3.0 |
| **백엔드** | Supabase (Postgrest + Edge Functions) | - |
| **백엔드 SDK** | Supabase-kt | 3.3.0 |
| **네트워크** | Ktor | 3.3.3 |
| **DI** | Koin | 4.1.1 |
| **네비게이션** | Navigation Compose | 2.9.2 |
| **로컬 DB** | Room KMP | - |
| **직렬화** | kotlinx-serialization | - |
| **날짜/시간** | kotlinx-datetime | - |
| **패키지명** | `org.ikseong.artech` | - |

### 1.3 지원 플랫폼

| 플랫폼 | 최소 버전 | 상태 |
|--------|-----------|------|
| Android | API 24 (7.0) | 활성 |
| iOS | 15.0 | 활성 |
| Desktop (JVM) | - | 제외 |
| Web (wasmJs) | - | 제외 (추후 확장) |

### 1.4 주요 기능

- 기술 블로그 아티클 피드 조회 (카테고리 필터, 키워드 검색, 페이지네이션)
- 아티클 상세 보기 (인앱 WebView)
- 즐겨찾기 (로컬 Room DB)
- 읽기이력 (로컬 Room DB, 날짜 그룹핑)
- 다크모드 설정

---

## 2. 화면 설계서

### 2.1 네비게이션 구조

하단 탭 네비게이션 (4탭):

```
┌─────────────────────────────────────┐
│                                     │
│         [현재 탭 화면 영역]          │
│                                     │
├─────────────────────────────────────┤
│  🏠 홈  │  ⭐ 즐겨찾기  │  🕐 이력  │  ⚙ 설정  │
└─────────────────────────────────────┘
```

| 탭 | Screen | 설명 |
|----|--------|------|
| 홈 | `HomeScreen` | 아티클 피드 리스트 |
| 즐겨찾기 | `FavoriteScreen` | 즐겨찾기한 아티클 목록 |
| 읽기이력 | `HistoryScreen` | 열람한 아티클 이력 |
| 설정 | `SettingsScreen` | 다크모드, 데이터 관리 |

추가 화면 (탭 내 네비게이션):

| Screen | 진입 경로 | 설명 |
|--------|-----------|------|
| `DetailScreen` | 아티클 카드 클릭 | WebView로 원문 표시 |

---

### 2.2 HomeScreen (홈 화면)

```
┌─────────────────────────────────────┐
│  Artech                             │
├─────────────────────────────────────┤
│  🔍 [검색어 입력...]               │  ← 검색바
├─────────────────────────────────────┤
│  [전체][Android][iOS][Web][...]     │  ← 카테고리 필터 (가로 스크롤)
├─────────────────────────────────────┤
│  ┌─────────────────────────────────┐│
│  │ 아티클 제목                     ││
│  │ 카카오 · Android · 2시간 전     ││
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │ 아티클 제목                     ││
│  │ 토스 · Server · 5시간 전        ││
│  └─────────────────────────────────┘│
│  ...                                │  ← 무한 스크롤
├─────────────────────────────────────┤
│  🏠 홈  │  ⭐ 즐겨찾기  │  🕐 이력  │  ⚙ 설정  │
└─────────────────────────────────────┘
```

#### 기능 명세

| 기능 | 설명 |
|------|------|
| 아티클 리스트 | 최신순 정렬된 아티클 목록 (LazyColumn) |
| 카테고리 필터 | 가로 스크롤 Chip으로 13개 카테고리 필터링 |
| 검색 | 제목/요약 키워드 검색 (ilike) |
| 무한 스크롤 | offset 기반 페이지네이션 |
| Pull to Refresh | 당겨서 새로고침 |
| 아티클 클릭 | DetailScreen으로 이동 + 읽기이력 자동 기록 |
| Loading 상태 | 초기 로딩, 추가 로딩 인디케이터 |
| Empty 상태 | 검색 결과 없음 / 카테고리 아티클 없음 |
| Error 상태 | 네트워크 에러 시 재시도 |

---

### 2.3 DetailScreen (상세 화면)

```
┌─────────────────────────────────────┐
│  [←]                    [🔗] [⭐]   │  ← TopBar (뒤로가기, 외부 브라우저, 즐겨찾기)
├─────────────────────────────────────┤
│                                     │
│         [WebView - 원문 표시]       │
│                                     │
│                                     │
│                                     │
└─────────────────────────────────────┘
```

#### 기능 명세

| 기능 | 설명 |
|------|------|
| WebView | 플랫폼별 expect/actual (Android: WebView, iOS: WKWebView) |
| 즐겨찾기 토글 | TopBar 액션으로 즐겨찾기 추가/제거 |
| 외부 브라우저 | 시스템 브라우저로 원문 URL 열기 |
| 공유 | 시스템 공유 시트 |

---

### 2.4 FavoriteScreen (즐겨찾기 화면)

```
┌─────────────────────────────────────┐
│  즐겨찾기                           │
├─────────────────────────────────────┤
│  ┌─────────────────────────────────┐│
│  │ 즐겨찾기한 아티클 제목          ││
│  │ 네이버 D2 · AI · 추가일시       ││
│  └─────────────────────────────────┘│
│  ┌─────────────────────────────────┐│
│  │ 즐겨찾기한 아티클 제목          ││
│  │ 우아한형제들 · Server · 추가일시 ││
│  └─────────────────────────────────┘│
│  ...                                │
├─────────────────────────────────────┤
│  🏠 홈  │  ⭐ 즐겨찾기  │  🕐 이력  │  ⚙ 설정  │
└─────────────────────────────────────┘
```

#### 기능 명세

| 기능 | 설명 |
|------|------|
| 즐겨찾기 목록 | Room DB에서 조회, 추가일시 역순 정렬 |
| 아티클 클릭 | DetailScreen으로 이동 |
| 스와이프 삭제 | 개별 즐겨찾기 삭제 |
| Empty 상태 | 즐겨찾기 없음 안내 |

---

### 2.5 HistoryScreen (읽기이력 화면)

```
┌─────────────────────────────────────┐
│  읽기이력                   [전체삭제]│
├─────────────────────────────────────┤
│  📅 오늘                            │
│  ┌─────────────────────────────────┐│
│  │ 읽은 아티클 제목                ││
│  │ 카카오 · Android · 10분 전      ││
│  └─────────────────────────────────┘│
│  📅 어제                            │
│  ┌─────────────────────────────────┐│
│  │ 읽은 아티클 제목                ││
│  │ 토스 · Server · 어제            ││
│  └─────────────────────────────────┘│
│  ...                                │
├─────────────────────────────────────┤
│  🏠 홈  │  ⭐ 즐겨찾기  │  🕐 이력  │  ⚙ 설정  │
└─────────────────────────────────────┘
```

#### 기능 명세

| 기능 | 설명 |
|------|------|
| 이력 목록 | 날짜별 그룹핑 (오늘, 어제, 이번 주, 이전) |
| 아티클 클릭 | DetailScreen으로 이동 |
| 전체 삭제 | 확인 다이얼로그 후 전체 이력 삭제 |
| 자동 기록 | 아티클 상세 진입 시 자동으로 이력 저장 |
| Empty 상태 | 읽기이력 없음 안내 |

---

### 2.6 SettingsScreen (설정 화면)

```
┌─────────────────────────────────────┐
│  설정                               │
├─────────────────────────────────────┤
│                                     │
│  🎨 테마                            │
│  ┌─────────────────────────────────┐│
│  │ 다크모드     [시스템 ▼]         ││  ← 시스템/라이트/다크
│  └─────────────────────────────────┘│
│                                     │
│  🗄️ 데이터 관리                     │
│  ┌─────────────────────────────────┐│
│  │ 즐겨찾기 전체 삭제              ││
│  ├─────────────────────────────────┤│
│  │ 읽기이력 전체 삭제              ││
│  └─────────────────────────────────┘│
│                                     │
│  ℹ️ 정보                            │
│  ┌─────────────────────────────────┐│
│  │ 앱 버전               v1.0.0   ││
│  └─────────────────────────────────┘│
│                                     │
├─────────────────────────────────────┤
│  🏠 홈  │  ⭐ 즐겨찾기  │  🕐 이력  │  ⚙ 설정  │
└─────────────────────────────────────┘
```

#### 기능 명세

| 기능 | 설명 |
|------|------|
| 다크모드 | 시스템 / 라이트 / 다크 선택 |
| 즐겨찾기 전체 삭제 | 확인 다이얼로그 후 삭제 |
| 읽기이력 전체 삭제 | 확인 다이얼로그 후 삭제 |
| 앱 버전 | 현재 앱 버전 표시 |

---

## 3. 데이터 모델

### 3.1 Supabase 테이블: tech_blog_articles

```sql
CREATE TABLE tech_blog_articles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title TEXT NOT NULL,
    link TEXT NOT NULL UNIQUE,
    summary TEXT,
    category article_category,
    blog_source TEXT NOT NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

#### article_category enum (13종)

```
AI, Android, Automation, Cross-platform, Data, DevOps, Hiring, Infra, iOS, PM, QA, Server, Web
```

#### blog_source (16개)

**국내**: 네이버 D2, 당근, 라인, 마켓컬리, 무신사, 쏘카, 여기어때, 요기요, 우아한형제들, 카카오, 쿠팡테크, 토스
**해외**: Airbnb Tech Blog, Google Developers, Meta Engineering, Netflix Tech Blog

### 3.2 로컬 DB (Room KMP)

#### FavoriteEntity

```kotlin
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val articleId: Long,
    val title: String,
    val link: String,
    val summary: String?,
    val category: String?,
    val blogSource: String,
    val publishedAt: String?,
    val favoritedAt: Long, // epoch millis
)
```

#### ReadHistoryEntity

```kotlin
@Entity(tableName = "read_history")
data class ReadHistoryEntity(
    @PrimaryKey val articleId: Long,
    val title: String,
    val link: String,
    val summary: String?,
    val category: String?,
    val blogSource: String,
    val publishedAt: String?,
    val readAt: Long, // epoch millis
)
```

### 3.3 도메인 모델

```kotlin
data class Article(
    val id: Long,
    val title: String,
    val link: String,
    val summary: String?,
    val category: ArticleCategory?,
    val blogSource: String,
    val publishedAt: Instant?,
    val createdAt: Instant?,
)

enum class ArticleCategory {
    AI, Android, Automation, CrossPlatform, Data, DevOps, Hiring, Infra, iOS, PM, QA, Server, Web;
}
```

---

## 4. 네비게이션 플로우

```
┌──────────────────────────────────────────────┐
│              Bottom Tab Navigation           │
│                                              │
│  ┌──────┐  ┌──────────┐  ┌───────┐  ┌────┐  │
│  │ Home │  │ Favorite │  │History│  │설정│  │
│  └──┬───┘  └────┬─────┘  └──┬────┘  └────┘  │
│     │           │           │                │
│     ▼           ▼           ▼                │
│  ┌──────────────────────────────┐            │
│  │        DetailScreen          │            │
│  │   (아티클 카드 클릭 시)       │            │
│  └──────────────────────────────┘            │
└──────────────────────────────────────────────┘
```

---

## 5. 프로젝트 구조

```
Artech/
├── composeApp/
│   └── src/
│       ├── commonMain/
│       │   └── kotlin/org/ikseong/artech/
│       │       ├── App.kt
│       │       ├── di/
│       │       │   └── AppModule.kt
│       │       ├── navigation/
│       │       │   └── AppNavigation.kt
│       │       ├── ui/
│       │       │   ├── screen/
│       │       │   │   ├── home/
│       │       │   │   │   ├── HomeScreen.kt
│       │       │   │   │   └── HomeViewModel.kt
│       │       │   │   ├── detail/
│       │       │   │   │   ├── DetailScreen.kt
│       │       │   │   │   └── DetailViewModel.kt
│       │       │   │   ├── favorite/
│       │       │   │   │   ├── FavoriteScreen.kt
│       │       │   │   │   └── FavoriteViewModel.kt
│       │       │   │   ├── history/
│       │       │   │   │   ├── HistoryScreen.kt
│       │       │   │   │   └── HistoryViewModel.kt
│       │       │   │   └── settings/
│       │       │   │       ├── SettingsScreen.kt
│       │       │   │       └── SettingsViewModel.kt
│       │       │   ├── component/
│       │       │   │   ├── ArticleCard.kt
│       │       │   │   ├── CategoryFilterRow.kt
│       │       │   │   ├── SearchBar.kt
│       │       │   │   └── EmptyState.kt
│       │       │   └── theme/
│       │       │       ├── Theme.kt
│       │       │       ├── Color.kt
│       │       │       └── Type.kt
│       │       ├── data/
│       │       │   ├── model/
│       │       │   │   ├── Article.kt
│       │       │   │   ├── ArticleDto.kt
│       │       │   │   └── ArticleCategory.kt
│       │       │   ├── repository/
│       │       │   │   ├── ArticleRepository.kt
│       │       │   │   ├── FavoriteRepository.kt
│       │       │   │   └── HistoryRepository.kt
│       │       │   ├── remote/
│       │       │   │   └── SupabaseProvider.kt
│       │       │   └── local/
│       │       │       ├── AppDatabase.kt
│       │       │       ├── FavoriteDao.kt
│       │       │       ├── FavoriteEntity.kt
│       │       │       ├── ReadHistoryDao.kt
│       │       │       └── ReadHistoryEntity.kt
│       │       └── util/
│       │           └── DateFormatter.kt
│       ├── androidMain/
│       └── iosMain/
├── iosApp/
├── docs/
│   └── SPECIFICATION.md
└── .claude/
    ├── CLAUDE.md
    └── rules/
        ├── architecture.md
        ├── supabase.md
        ├── issue-workflow.md
        ├── pr-workflow.md
        └── implementation-strategy.md
```

---

## 6. API 패턴 (Supabase Postgrest)

### 아티클 목록 조회 (페이지네이션)

```kotlin
supabase.from("tech_blog_articles")
    .select()
    .order("published_at", Order.DESCENDING)
    .range(from = offset.toLong(), to = (offset + limit - 1).toLong())
```

### 카테고리 필터링

```kotlin
supabase.from("tech_blog_articles")
    .select()
    .eq("category", category.name)
    .order("published_at", Order.DESCENDING)
    .range(from = offset.toLong(), to = (offset + limit - 1).toLong())
```

### 키워드 검색

```kotlin
supabase.from("tech_blog_articles")
    .select()
    .or("title.ilike.%$keyword%,summary.ilike.%$keyword%")
    .order("published_at", Order.DESCENDING)
```

---

## 7. 구현 마일스톤

### Phase 0: Foundation Setup
- [ ] develop 브랜치 생성
- [ ] 핵심 의존성 추가 + Desktop/Web 타겟 제거
- [ ] Supabase 클라이언트 초기화 + Koin DI 설정

### Phase 1: Data Layer
- [ ] Article 도메인 모델 및 DTO 정의
- [ ] ArticleRepository 구현

### Phase 2: Navigation + Home Screen
- [ ] 앱 테마 및 디자인 시스템
- [ ] Bottom Tab Navigation 구조
- [ ] ArticleCard 공통 컴포넌트
- [ ] HomeScreen 구현

### Phase 3: Detail Screen
- [ ] 플랫폼별 WebView expect/actual
- [ ] DetailScreen 구현

### Phase 4: Favorites & History (Room KMP)
- [ ] Room KMP 로컬 DB 설정
- [ ] FavoriteRepository + FavoriteScreen
- [ ] HistoryRepository + HistoryScreen

### Phase 5: Settings
- [ ] 다크모드 및 앱 설정 저장
- [ ] SettingsScreen

### Phase 6: Polish
- [ ] 정렬 기능 + 수집 방식 탭
- [ ] 에러 핸들링 통합 및 UI 폴리싱
