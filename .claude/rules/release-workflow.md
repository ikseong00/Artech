# 릴리즈 워크플로우

## 배포 흐름

```
develop → feature branch → PR → develop → 버전 bump PR → 태그 push → CD 자동 배포
```

## 릴리즈 체크리스트

### 1. 릴리즈 노트 업데이트

`whatsnew/whatsnew-ko-KR` 파일에 변경 내역 작성 (Android Play Store에 표시됨)

```
whatsnew/
  whatsnew-ko-KR   ← 한국어 릴리즈 노트
  whatsnew-en-US   ← 영어 릴리즈 노트
```

iOS 릴리즈 노트는 App Store Connect에서 심사 제출 시 직접 입력.

### 2. 버전 bump

브랜치 생성 → 버전 수정 → PR (prefix: `Release`) → develop 머지

**Android** (`composeApp/build.gradle.kts`)
```kotlin
versionCode = +1      // 항상 1씩 증가
versionName = "x.x.x"
```

**iOS** (`iosApp/Configuration/Config.xcconfig`)
```
CURRENT_PROJECT_VERSION = versionCode와 동일하게
MARKETING_VERSION = x.x.x
```

### 3. 태그 push → CD 자동 트리거

```bash
# Android → Play Store (alpha 트랙)
git tag release/android-vx.x.x
git push origin release/android-vx.x.x

# iOS → App Store Connect (TestFlight)
git tag release/ios-vx.x.x
git push origin release/ios-vx.x.x
```

## CD 구성

| 항목 | Android | iOS |
|------|---------|-----|
| 트리거 | `release/android-*` 태그 | `release/ios-*` 태그 |
| 배포 대상 | Play Store alpha 트랙 | App Store Connect |
| 서명 | keystore.jks (Secret) | Apple Distribution 인증서 (Secret) |
| 릴리즈 노트 | `whatsnew/` 폴더 | App Store Connect에서 직접 입력 |

## GitHub Secrets 목록

| Secret | 용도 |
|--------|------|
| `KEYSTORE_FILE` | Android 릴리즈 서명 키스토어 (base64) |
| `KEY_ALIAS` | 키 alias |
| `KEY_PASSWORD` | 키 비밀번호 |
| `STORE_PASSWORD` | 키스토어 비밀번호 |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Play Store API 서비스 계정 |
| `APPLE_DIST_CERT_P12` | iOS 배포 인증서 (base64) |
| `APPLE_DIST_CERT_PASSWORD` | 인증서 비밀번호 |
| `PROVISIONING_PROFILE` | App Store 배포 프로파일 (base64) |
| `APP_STORE_CONNECT_API_KEY_ID` | ASC API Key ID |
| `APP_STORE_CONNECT_ISSUER_ID` | ASC Issuer ID |
| `APP_STORE_CONNECT_PRIVATE_KEY` | ASC API 개인키 (.p8) |

## iOS 배포 후

App Store Connect → TestFlight에 빌드 올라옴 → 직접 "App Store에 제출" 버튼 눌러야 심사 진행
