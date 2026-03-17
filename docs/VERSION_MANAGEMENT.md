# 버전 관리 프로세스

## 개요

앱 업데이트 팝업은 Supabase `app_versions` 테이블을 기반으로 동작합니다.
앱 실행 시 현재 버전과 서버의 버전 정보를 비교하여 필수/선택적 업데이트를 안내합니다.

## app_versions 테이블 구조

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | bigint | PK |
| platform | text | `android` 또는 `ios` |
| force_update_version | text | 필수 업데이트 최소 버전 |
| optional_update_version | text | 선택적 업데이트 최소 버전 |
| store_url | text | 스토어 URL |

## 업데이트 판단 로직

1. **필수 업데이트 (FORCE)**: 현재 버전 < `force_update_version` -> 앱 사용 차단, 업데이트 필수
2. **선택적 업데이트 (OPTIONAL)**: 현재 버전 < `optional_update_version` -> 업데이트 권유, "나중에" 가능
3. **최신 (NONE)**: 업데이트 불필요

## 릴리즈 시 버전 관리

### 매 배포마다 (필수)

`optional_update_version`을 배포 버전과 동일하게 업데이트합니다.

```sql
-- Android 예시 (v1.1.0 배포 시)
UPDATE app_versions
SET optional_update_version = '1.1.0'
WHERE platform = 'android';

-- iOS 예시 (v1.1.0 배포 시)
UPDATE app_versions
SET optional_update_version = '1.1.0'
WHERE platform = 'ios';
```

### 필수 업데이트가 필요한 경우 (수동)

Breaking change, 보안 패치 등 반드시 업데이트가 필요한 경우에만 `force_update_version`을 설정합니다.

```sql
-- Android 필수 업데이트 설정
UPDATE app_versions
SET force_update_version = '1.1.0'
WHERE platform = 'android';

-- iOS 필수 업데이트 설정
UPDATE app_versions
SET force_update_version = '1.1.0'
WHERE platform = 'ios';
```

## 주의사항

- `force_update_version`은 신중하게 설정할 것 (이전 버전 사용자가 앱을 전혀 사용할 수 없게 됨)
- 네트워크 오류 시 업데이트 체크를 건너뛰므로 앱 사용이 차단되지 않음
- 버전 비교는 시맨틱 버전 (Major.Minor.Patch) 기준으로 각 파트를 정수 비교
