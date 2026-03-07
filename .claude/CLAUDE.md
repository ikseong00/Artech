# Artech 프로젝트 지침

## 프로젝트 개요

- **개발 기술 블로그 애그리게이터 앱** (Compose Multiplatform)
- 백엔드: Supabase (Postgrest로 아티클 조회, Edge Function으로 수집)
- 로컬 저장소: Room KMP (즐겨찾기, 읽기이력)
- **앱 우선 개발** (Android, iOS) → Web은 추후 확장

## 기술 스택

| 구분 | 기술 | 버전 |
|------|------|------|
| UI | Compose Multiplatform | 1.10.0 |
| Language | Kotlin | 2.3.0 |
| Backend SDK | Supabase-kt | 3.3.0 |
| Network | Ktor | 3.3.3 |
| DI | Koin | 4.1.1 |
| Navigation | Navigation Compose | 2.9.2 |
| Local DB | Room KMP | - |
| Serialization | kotlinx-serialization | - |
| DateTime | kotlinx-datetime | - |
| Coroutines | kotlinx-coroutines | 1.10.2 |

## 타겟 플랫폼

| 플랫폼 | 최소 버전 | 상태 |
|--------|-----------|------|
| Android | API 24 (7.0) | 활성 |
| iOS | 15.0 | 활성 |
| Desktop (JVM) | - | **제외** |
| Web (wasmJs) | - | **제외** (추후 확장) |

## 아키텍처

- **MVVM + Repository 패턴**
- `composeApp` 중심 개발 (commonMain에 모든 공유 로직)
- 상세 구조는 `.claude/rules/architecture.md` 참조

## 코드 스타일

- Kotlin 공식 코딩 컨벤션 준수
- Composable 함수는 PascalCase
- 변수/함수는 camelCase

## Git 커밋

- Co-Authored-By 또는 Claude 관련 내용 절대 포함하지 않음
- 커밋 메시지는 한글 또는 영문으로 간결하게 작성

### Prefix

`feat`, `fix`, `refactor`, `docs`, `ci`, `chore`

### 커밋 예시

```
[feat] #12: 로그인 화면 구현

- 이메일/비밀번호 입력 필드 추가
- 로그인 버튼 구현
- 유효성 검사 로직 추가
```

## Git 워크플로우

### 브랜치 전략

```
main (프로덕션) ← develop (개발 통합) ← feature branches
```

### 브랜치 네이밍

```
{type}/#{issue}-{kebab-description}
```

예: `feat/#9-home-screen`, `fix/#15-category-filter-crash`

### 상세 규칙

- 이슈/PR 워크플로우: `.claude/rules/issue-workflow.md`, `.claude/rules/pr-workflow.md` 참조

## 이슈

### 이슈 제목 예시

```
[Feat]: 로그인 화면 구현
```

## Pull Request

- PR은 항상 `develop` 브랜치로 생성
- base 브랜치: `develop`

### PR 제목 예시

```
[Feat] #12: 로그인 화면 구현
```

## 빌드 명령어

```bash
# Android
./gradlew :composeApp:assembleDebug

# iOS Framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

## Supabase DB 참고

- **Project ID**: `iqbygidnorgcxxcdmbqm`
- **테이블**: `tech_blog_articles` (276행, RLS 활성)
- **카테고리 enum**: AI, Android, Automation, Cross-platform, Data, DevOps, Hiring, Infra, iOS, PM, QA, Server, Web
- 상세 스키마 및 쿼리 패턴: `.claude/rules/supabase.md` 참조

## 프로젝트 구조

- `composeApp/` - Compose Multiplatform UI (commonMain 중심)
- `iosApp/` - iOS Xcode 프로젝트
- `docs/` - 프로젝트 명세서
- `.claude/rules/` - 아키텍처, 워크플로우, 구현 전략 가이드

## 패키지

- `org.ikseong.artech`

## 역할 자동 분배

유저의 자연어 요청을 분석하여 적합한 역할을 자동으로 판단하고, 해당 역할의 skill(`.claude/skills/`)이 자동으로 활성화되어 전문가로서 동작한다.

| 맥락 키워드 | 역할 | Skill |
|-------------|------|-------|
| 기획, PRD, 요구사항, 유저스토리, 기능정의 | 기획 전문가 | `/prd` |
| UI, 화면, 디자인, 컴포넌트, 레이아웃 | 디자인 전문가 | `/design` |
| 구현, 개발, 코딩, 기능추가, 버그수정 | 개발 전문가 | `/dev` |
| 테스트, 검증, 커버리지, TDD | 테스트 전문가 | `/test` |
| 문서, README, KDoc, 가이드, 설명 | 문서화 전문가 | `/docs` |
| 리뷰, 코드품질, 점검, 검토 | 코드 리뷰 전문가 | `/review` |

### 동작 방식

1. 유저 요청 수신 → 맥락에서 역할 판단 → 해당 skill 자동 활성화
2. 명시적 `/skill` 호출 시에도 동일하게 동작 (예: `/dev #4`, `/review`, `/prd 홈 화면`)
3. 해당 전문가로서 작업 수행
4. 여러 역할이 필요한 경우 순차 또는 병렬(sub-agent)로 조합

### 규칙

- 역할 판단은 유연하게 (키워드 매칭이 아닌 맥락 기반)
- 역할이 불분명한 경우 유저에게 확인
- 하나의 요청에 여러 역할이 겹치면 주 역할 기준으로 동작하되, 필요시 다른 역할 가이드도 참조
