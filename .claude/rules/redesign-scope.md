# Issue #39: Stitch 기반 리디자인 확정 범위

## Stitch 디자인 참조
- 프로젝트: "Category Selection Onboarding" (ID: `9173371430471866189`)
- 12개 스크린 (다크모드 6 + 라이트모드 6)

## 색상 체계 (Blue → Cyan Teal)

### Dark Mode
- Primary: `#00D1FF` (현재 `#A4C9FF`)
- Background: `#0D1B1E` (현재 Material3 기본)
- Surface: `#15262a` ~ `#16282c`
- Text Secondary: `#9ab6bc` ~ `#8FA3A8`
- Border: `#27373a`

### Light Mode

- Primary: `#00B8D4` 또는 `#00D1FF`
- Background: `#FFFFFF` (현재 Material3 기본)
- Surface: `#F1F5F9` ~ `#F8FAFC`
- Text Secondary: `#64748B`

## 화면별 변경 사항

### HomeScreen
- "오늘의 추천" 가로 스크롤 섹션 추가
- 아티클 카드 → 가로 레이아웃 + 썸네일(96x96)

### ArticleCard
- 가로 레이아웃: 텍스트(좌) + 썸네일(우)
- 그림자 추가 (subtle shadow)
- 출처 + 시간 하단 배치

### FavoriteScreen
- 카테고리 필터 칩 추가
- 카드 내 북마크 토글 버튼

### HistoryScreen
- 카드 내 썸네일 추가 (80x80)
- 삭제 → 텍스트 버튼 ("전체 삭제")
- 그룹 헤더 크기 키움

### SettingsScreen
- 다크모드: 토글 스위치로 변경
- 설정 항목: chevron_right 아이콘 추가
- 아이콘 박스 스타일 (배경색 있는 둥근 아이콘)
- 삭제 아이콘: 빨간색
- 시스템 폰트 설정 제거
- 앱 버전 표시: 현재 디자인 유지

### DetailScreen
- WebView 유지 (변경 없음)
- 색상만 테마에 맞춰 변경

### Bottom Navigation
- 4탭 유지: 홈/즐겨찾기/히스토리/설정
- Stitch 디자인 스타일 적용 (glow effect 등)

## 미구현 (기억만)
- **관심 카테고리 설정**: 유저 기능(인증) 없어서 UI 미적용. 추후 유저 기능 추가 시 구현
  - 카테고리 선택 바텀시트 디자인은 Stitch에 있음
  - 설정 화면에서 접근 예정

## Key Decisions
- DetailScreen: WebView 유지 (Stitch의 인앱 렌더링 방식 채택 안함)
- 시스템 폰트 설정: 제거
- 설정 탑바: 뒤로가기 없음 (현재 유지)
- 바텀 탭: 홈/즐겨찾기/히스토리/설정 (4탭)
- 앱 버전: 현재 디자인 유지
