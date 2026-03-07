 # 아키텍처 가이드

## 패턴: MVVM + Repository

```
Screen (Composable) → ViewModel (StateFlow) → Repository → DataSource (Remote/Local)
```

- **Screen**: UI 렌더링만 담당. ViewModel에서 상태를 collect
- **ViewModel**: UI 상태 관리, 비즈니스 로직 호출. `StateFlow<UiState>` 노출
- **Repository**: 데이터 접근 추상화. Remote/Local 데이터소스 조합
- **DataSource**: Supabase(Remote), Room(Local) 직접 접근

## 패키지 구조

`composeApp/src/commonMain/kotlin/org/ikseong/artech/` 기준:

```
org.ikseong.artech/
├── di/              → Koin 모듈 정의
├── navigation/      → NavHost, Route sealed interface
├── ui/
│   ├── screen/
│   │   ├── home/        → HomeScreen, HomeViewModel, HomeUiState
│   │   ├── detail/      → DetailScreen, DetailViewModel
│   │   ├── favorite/    → FavoriteScreen, FavoriteViewModel
│   │   ├── history/     → HistoryScreen, HistoryViewModel
│   │   └── settings/    → SettingsScreen, SettingsViewModel
│   ├── component/       → ArticleCard, CategoryFilterRow, SearchBar, EmptyState
│   └── theme/           → Theme, Color, Type
├── data/
│   ├── model/           → Article, ArticleDto, ArticleCategory enum, Favorite, ReadHistory
│   ├── repository/      → ArticleRepository, FavoriteRepository, HistoryRepository
│   ├── remote/          → SupabaseProvider
│   └── local/           → Room Database, DAO, Entity
└── util/                → DateFormatter 등 유틸리티
```

## 네이밍 컨벤션

| 구분 | 규칙 | 예시 |
|------|------|------|
| Screen | `{Feature}Screen` | `HomeScreen`, `DetailScreen` |
| ViewModel | `{Feature}ViewModel` | `HomeViewModel` |
| UiState | `{Feature}UiState` | `HomeUiState` |
| Repository | `{Feature}Repository` | `ArticleRepository` |
| DTO | `{Model}Dto` | `ArticleDto` |
| Entity (Room) | `{Model}Entity` | `FavoriteEntity` |
| DAO | `{Model}Dao` | `FavoriteDao` |
| Koin Module | `{layer}Module` | `dataModule`, `viewModelModule` |

## 상태 관리

```kotlin
// UiState 패턴
data class HomeUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: ArticleCategory? = null,
)

// ViewModel에서 StateFlow 사용
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}

// Screen에서 collectAsState
@Composable
fun HomeScreen(viewModel: HomeViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
}
```

## 의존성 주입 (Koin)

- `dataModule`: Repository, DataSource, Supabase 클라이언트
- `viewModelModule`: 모든 ViewModel
- 플랫폼별 모듈: `platformModule` (expect/actual)