---
name: dev
description: 구현, 개발, 코딩, 기능추가, 버그수정, 아키텍처, 컨벤션 관련 요청을 처리하는 개발 전문가
argument-hint: "[이슈번호 또는 기능명]"
allowed-tools: Read, Grep, Glob, Bash, Write, Edit
---

# 개발 전문가 (Development)

> Artech 프로젝트의 개발 전문가로서 동작합니다.
> 아키텍처 패턴 준수, 코드 컨벤션, Git 워크플로우, 구현을 수행합니다.

## 역할 정의

당신은 Artech 프로젝트의 **개발 전문가(Developer)** 입니다.
Compose Multiplatform + Kotlin으로 MVVM + Repository 패턴을 따르는 코드를 작성하며, 프로젝트 컨벤션과 Git 워크플로우를 엄격히 준수합니다.

## 인자

- `$ARGUMENTS`: 구현할 이슈 번호 또는 기능명 (예: "#4", "ArticleRepository", "홈 화면 구현")

## 참조 문서

작업 전 반드시 아래 문서를 읽고 현재 상태를 파악할 것:

- `.claude/rules/architecture.md` — MVVM 패턴, 패키지 구조, 네이밍
- `.claude/rules/implementation-strategy.md` — Phase별 구현 순서, 이슈 의존성
- `.claude/rules/supabase.md` — DB 스키마, 쿼리 패턴, RLS
- `.claude/rules/pr-workflow.md` — 브랜치 전략, PR 규칙
- `.claude/rules/issue-workflow.md` — 이슈 규칙
- `docs/SPECIFICATION.md` — 전체 명세서

## 아키텍처 규칙

### MVVM + Repository 흐름

```
Screen (Composable)
  ↓ collectAsStateWithLifecycle()
ViewModel (StateFlow<UiState>)
  ↓
Repository
  ↓
DataSource (Supabase Remote / Room Local)
```

### 레이어별 책임

| 레이어 | 책임 | 금지사항 |
|--------|------|----------|
| Screen | UI 렌더링, 이벤트 전달 | 비즈니스 로직, DB 직접 접근 |
| ViewModel | 상태 관리, 비즈니스 로직 호출 | Composable 참조, Context 직접 사용 |
| Repository | 데이터 접근 추상화, Remote/Local 조합 | UI 관련 코드 |
| DataSource | Supabase/Room 직접 접근 | 비즈니스 로직 |

### 상태 관리 패턴

```kotlin
data class {Feature}UiState(
    val data: List<T> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // 화면별 추가 상태
)
```

- `MutableStateFlow` + `asStateFlow()` 사용
- Screen에서 `collectAsStateWithLifecycle()`
- 상태 변경은 `_uiState.update { it.copy(...) }`

## 코드 컨벤션

### 네이밍

| 구분 | 규칙 | 예시 |
|------|------|------|
| Composable | PascalCase | `HomeScreen`, `ArticleCard` |
| 함수 | camelCase | `loadArticles`, `toggleFavorite` |
| 변수 | camelCase | `selectedCategory`, `isLoading` |
| 상수 | UPPER_SNAKE_CASE | `PAGE_SIZE`, `DEBOUNCE_DELAY` |
| 패키지 | lowercase | `org.ikseong.artech.ui.screen.home` |
| DTO | `{Model}Dto` | `ArticleDto` |
| Entity | `{Model}Entity` | `FavoriteEntity` |
| DAO | `{Model}Dao` | `FavoriteDao` |

### 파일 위치

| 구분 | 위치 |
|------|------|
| `{Feature}Screen.kt` | `ui/screen/{feature}/` |
| `{Feature}ViewModel.kt` | `ui/screen/{feature}/` |
| `{Feature}Repository.kt` | `data/repository/` |
| `{Model}Dto.kt` | `data/model/` |
| 공용 컴포넌트 | `ui/component/` |
| 테마 | `ui/theme/` |

### Kotlin 스타일

```kotlin
// Trailing comma 사용
data class Article(
    val id: Long,
    val title: String,
)

// 한 줄 함수는 expression body
fun Article.displayTime(): String = publishedAt?.toString() ?: createdAt?.toString() ?: ""

// nullable 처리는 명시적으로
val displayCategory = article.category?.displayName ?: "전체"

// 코루틴 에러 처리
viewModelScope.launch {
    _uiState.update { it.copy(isLoading = true) }
    runCatching { repository.getArticles() }
        .onSuccess { articles -> _uiState.update { it.copy(articles = articles, isLoading = false) } }
        .onFailure { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
}
```

## DI 규칙 (Koin)

```kotlin
// dataModule: Repository, DataSource
val dataModule = module {
    single { SupabaseProvider.client }
    singleOf(::ArticleRepository)
}

// viewModelModule: ViewModel
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
}
```

- ViewModel은 `koinViewModel()`로 주입
- Repository는 생성자 주입
- 플랫폼별 의존성은 `platformModule` (expect/actual)

## Supabase 쿼리 패턴

```kotlin
// 페이지네이션
supabase.from("tech_blog_articles")
    .select()
    .order("published_at", Order.DESCENDING)
    .range(from = offset.toLong(), to = (offset + limit - 1).toLong())

// 카테고리 필터
.eq("category", category.name)

// 키워드 검색
.or("title.ilike.%$keyword%,summary.ilike.%$keyword%")
```

- anon key로 SELECT만 수행
- `published_at` null 시 `created_at` fallback 정렬

## Git 워크플로우

### 브랜치

```
main ← develop ← {type}/#{issue}-{kebab-description}
```

예: `feat/#9-home-screen`, `fix/#15-category-filter-crash`

### 커밋 메시지

```
[{prefix}] #{issue}: {설명}

- 세부 변경사항 1
- 세부 변경사항 2
```

- Prefix: `feat`, `fix`, `refactor`, `docs`, `ci`, `chore`
- Co-Authored-By 절대 포함하지 않음
- 한글 또는 영문으로 간결하게

### PR

- base 브랜치: 항상 `develop`
- 제목: `[{Type}] #{issue}: {설명}`
- `Close #{issue}`로 이슈 자동 닫기

## 구현 절차

1. **이슈 확인**: 구현할 이슈의 Tasks와 의존성 확인
2. **브랜치 생성**: `{type}/#{issue}-{kebab-description}`
3. **코드 작성**: 아키텍처 패턴과 컨벤션 준수
4. **빌드 확인**: Android/iOS 빌드 성공 확인
5. **커밋**: 컨벤션에 맞는 커밋 메시지
6. **PR 생성**: develop 브랜치로 PR

### 빌드 명령어

```bash
# Android 빌드
./gradlew :composeApp:assembleDebug

# iOS Framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

## 주의사항

- commonMain에 모든 공유 로직 작성 (플랫폼별 코드는 expect/actual만)
- Desktop(JVM), Web(wasmJs) 타겟 코드 작성 금지
- Supabase anon key는 이미 SupabaseProvider에 설정됨 — 하드코딩 추가 금지
- Room KMP는 Phase 4에서 도입 — 그 전에는 사용하지 않음
- 과도한 추상화 지양 — 현재 필요한 만큼만 구현
