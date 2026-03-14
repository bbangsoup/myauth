# 관리자 어드민 기능 구현 설계 문서

## 1. 문서 목적

이 문서는 현재 `myauth` 프로젝트를 분석한 뒤, 관리자 어드민 기능을 백엔드 기준으로 안정적으로 구현하기 위한 설계 초안을 정리한 문서다.

목표는 다음 3가지다.

1. 현재 프로젝트에서 재사용 가능한 구조를 식별한다.
2. 관리자 기능 구현에 필요한 추가 엔티티/API/권한 정책을 정의한다.
3. 실제 개발 순서를 단계별로 제안한다.

---

## 2. 현재 프로젝트 분석 요약

### 2.1 기술 스택

- Java 17
- Spring Boot 4.0.0
- Spring Security
- Spring Data JPA
- MySQL
- JWT 기반 인증

### 2.2 현재 구현된 핵심 도메인

현재 프로젝트는 "인증 + SNS형 커뮤니티 백엔드" 구조에 가깝다.

- 사용자: `User`, `UserProfile`
- 인증: 회원가입, 로그인, 리프레시 토큰, 카카오 OAuth
- 게시물: `Post`, `PostImage`, `PostHashtag`, `Mention`
- 상호작용: `Comment`, `Like`, `Bookmark`, `Follow`
- 피드/검색: 해시태그, 추천/인기/조회수 피드

### 2.3 관리자 기능 관점에서 이미 준비된 요소

현재 코드에는 관리자 기능을 위한 기반이 일부 이미 존재한다.

#### 사용자 권한 필드 존재

`User` 엔티티에 아래 필드가 있다.

- `role`: `ROLE_ADMIN`, `ROLE_USER`
- `status`: `ACTIVE`, `DELETED`, `INACTIVE`, `PENDING_VERIFICATION`, `SUSPENDED`
- `isActive`
- `isSuperUser`
- `accountLockedUntil`
- `failedLoginAttempts`

즉, "관리자 계정", "정지 계정", "비활성 계정" 개념은 도메인상 이미 반영돼 있다.

#### 게시물/댓글 Soft Delete 구조 존재

- `Post`는 `isDeleted` 기반 soft delete 지원
- 댓글도 삭제 로직이 별도 서비스로 관리되고 있음

이는 관리자 삭제/숨김 기능을 구현하기 좋은 출발점이다.

#### 전역 예외 처리 구조 존재

`GlobalExceptionHandler`가 있어 관리자 기능용 예외(`AdminAccessDeniedException`, `ModerationPolicyException`)를 쉽게 추가할 수 있다.

### 2.4 현재 부족한 부분

관리자 기능 구현을 위해 아래 요소는 아직 없다.

- 관리자 전용 API namespace
- 역할 기반 세부 권한 분리
- 신고(Report) 도메인
- 제재 이력(Suspension/Ban/Moderation Action) 도메인
- 관리자 감사 로그(Audit Log)
- 관리자 대시보드 통계 API
- 관리자 전용 검색/필터링용 Repository 메서드

즉, 현재는 "관리자가 될 수 있는 사용자 구조"는 있으나 "관리자가 실제 운영할 도구"는 없는 상태다.

---

## 3. 권장 어드민 기능 범위

관리자 기능은 1차와 2차로 나누어 구현하는 것을 권장한다.

### 3.1 1차 구현 범위(MVP)

운영에 바로 필요한 최소 기능이다.

- 관리자 로그인 접근 제어
- 관리자 대시보드 요약 통계
- 사용자 목록 조회/검색/상세 조회
- 사용자 상태 변경
  - 활성화
  - 비활성화
  - 정지
  - 관리자 승격/회수
- 게시물 목록 조회/검색/상세 조회
- 게시물 강제 삭제(soft delete)
- 댓글 강제 삭제
- 관리자 작업 이력 저장

### 3.2 2차 구현 범위

커뮤니티 운영 품질을 높이는 기능이다.

- 신고 접수/처리 시스템
- 신고 사유별 분류 및 상태 관리
- 제재 기간 관리
- 관리자 메모
- 벌크 액션
  - 다수 사용자 정지
  - 다수 게시물 숨김
- 운영 통계 고도화
  - 일별 가입 수
  - 일별 게시물 수
  - 신고 처리율

### 3.3 3차 구현 범위

운영 자동화/보안 강화 영역이다.

- 권한 세분화
  - 슈퍼 관리자
  - 운영 관리자
  - 콘텐츠 관리자
- IP/디바이스 기반 이상 탐지
- 자동 제재 룰
- 관리자 액션 알림
- 외부 대시보드 연동

---

## 4. 권한 모델 제안

### 4.1 현재 권한 모델

현재는 사실상 아래 2개 역할만 있다.

- `ROLE_USER`
- `ROLE_ADMIN`

### 4.2 권장 확장 방향

초기에는 기존 `ROLE_ADMIN`을 그대로 활용하고, 추후 필요 시 세분화한다.

#### 초기 운영 권장안

- `ROLE_USER`: 일반 사용자
- `ROLE_ADMIN`: 관리자
- `isSuperUser = true`: 최고 관리자

#### 중장기 확장안

`User.Role`을 아래처럼 확장하는 방식을 고려할 수 있다.

- `ROLE_USER`
- `ROLE_ADMIN`
- `ROLE_SUPER_ADMIN`
- `ROLE_CONTENT_MANAGER`
- `ROLE_SUPPORT_MANAGER`

다만 현재 프로젝트 구조상 1차 구현에서는 enum 확장보다 `ROLE_ADMIN + isSuperUser` 조합이 비용 대비 효율이 높다.

### 4.3 관리자 접근 정책

관리자 API는 최소 아래 정책이 필요하다.

- `/api/admin/**` 경로는 `ROLE_ADMIN` 이상만 허용
- 관리자 승격/회수는 `isSuperUser = true`만 허용
- 본인 계정 삭제/강등 금지
- 마지막 슈퍼관리자 강등 금지

---

## 5. API 구조 제안

관리자 API는 일반 사용자 API와 완전히 분리하는 것이 좋다.

### 5.1 권장 경로

- `/api/admin/dashboard`
- `/api/admin/users`
- `/api/admin/posts`
- `/api/admin/comments`
- `/api/admin/reports`
- `/api/admin/audit-logs`

### 5.2 SecurityConfig 변경 방향

현재 `SecurityConfig`는 대부분 경로를 `authenticated()`로 보호한다.
관리자 기능 추가 시 아래 규칙을 먼저 넣는 것이 좋다.

```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

주의할 점:

- Spring Security의 `hasRole("ADMIN")`은 실제 권한 값 `ROLE_ADMIN`과 매핑된다.
- 관리자 세부 권한이 필요하면 `hasAuthority(...)` 또는 서비스 레벨 검증을 추가한다.

---

## 6. 엔티티 설계 제안

1차 구현에 필요한 최소 엔티티는 아래 2개다.

- `AdminAuditLog`
- 선택 사항: `UserSuspensionHistory`

2차부터는 아래도 추가한다.

- `Report`
- `ReportTarget`
- `ModerationAction`

### 6.1 AdminAuditLog

관리자가 무엇을 했는지 남기는 이력 테이블이다.

#### 목적

- 누가 어떤 사용자/게시물/댓글에 어떤 조치를 했는지 추적
- 운영 사고 대응
- 관리자 화면 활동 로그 조회

#### 권장 컬럼

- `id`
- `admin_user_id`
- `action_type`
- `target_type`
- `target_id`
- `reason`
- `before_data`
- `after_data`
- `created_at`

#### action_type 예시

- `USER_SUSPENDED`
- `USER_ACTIVATED`
- `USER_ROLE_CHANGED`
- `POST_DELETED`
- `COMMENT_DELETED`
- `REPORT_RESOLVED`

#### target_type 예시

- `USER`
- `POST`
- `COMMENT`
- `REPORT`

### 6.2 UserSuspensionHistory

사용자 정지/복구 이력을 분리 관리하는 테이블이다.

#### 목적

- 현재 `User.status = SUSPENDED`만으로는 "왜 정지됐는지", "언제 해제되는지" 추적이 약함
- 운영 이력과 정책 적용을 명확히 만들 수 있음

#### 권장 컬럼

- `id`
- `user_id`
- `admin_user_id`
- `reason`
- `start_at`
- `end_at`
- `is_active`
- `created_at`

### 6.3 Report

2차 구현용 신고 테이블이다.

#### 권장 컬럼

- `id`
- `reporter_user_id`
- `target_type`
- `target_id`
- `reason_type`
- `reason_detail`
- `status`
- `reviewed_by`
- `reviewed_at`
- `created_at`

#### status 예시

- `PENDING`
- `IN_REVIEW`
- `RESOLVED`
- `REJECTED`

---

## 7. 관리자 기능별 상세 설계

## 7.1 관리자 대시보드

### 목적

운영자가 서비스 상태를 한 화면에서 빠르게 파악할 수 있도록 한다.

### 1차 지표

- 전체 사용자 수
- 활성 사용자 수
- 정지 사용자 수
- 전체 게시물 수
- 삭제되지 않은 게시물 수
- 전체 댓글 수

### 권장 API

`GET /api/admin/dashboard/summary`

### 응답 예시

```json
{
  "success": true,
  "message": "관리자 대시보드 요약 조회 성공",
  "data": {
    "totalUsers": 1200,
    "activeUsers": 1100,
    "suspendedUsers": 35,
    "inactiveUsers": 20,
    "totalPosts": 8400,
    "activePosts": 8001,
    "deletedPosts": 399,
    "totalComments": 15200
  }
}
```

### Repository 구현 포인트

- `UserRepository.countByStatus(...)`
- `UserRepository.countByIsActiveTrue()`
- `PostRepository.countByIsDeletedFalse()`
- `CommentRepository.count()`

---

## 7.2 사용자 관리

### 주요 기능

- 사용자 목록 조회
- 이메일/이름/상태 검색
- 사용자 상세 조회
- 상태 변경
- 관리자 권한 변경

### 권장 API

- `GET /api/admin/users`
- `GET /api/admin/users/{userId}`
- `PATCH /api/admin/users/{userId}/status`
- `PATCH /api/admin/users/{userId}/role`

### 목록 조회 필터

- `keyword`
- `status`
- `role`
- `provider`
- `isActive`
- `createdFrom`
- `createdTo`

### 상태 변경 요청 예시

```json
{
  "status": "SUSPENDED",
  "reason": "욕설 및 반복 신고",
  "lockedUntil": "2026-04-01T00:00:00"
}
```

### 서버 처리 규칙

- `SUSPENDED` 변경 시 `accountLockedUntil` 설정 가능
- `ACTIVE` 복구 시 `accountLockedUntil = null`
- `DELETED`는 즉시 물리 삭제 대신 운영 상태 전환 권장
- 변경 전후 상태를 `AdminAuditLog`에 기록

### 추가 검증 규칙

- 본인 계정 role/status 변경 제한
- 마지막 슈퍼관리자 권한 제거 금지
- 일반 관리자는 다른 관리자 계정 수정 제한 가능

---

## 7.3 게시물 관리

### 주요 기능

- 게시물 목록 조회
- 작성자/내용 기반 검색
- 삭제 여부 필터
- 강제 삭제
- 추후 복구 기능 고려

### 권장 API

- `GET /api/admin/posts`
- `GET /api/admin/posts/{postId}`
- `DELETE /api/admin/posts/{postId}`

### 목록 필터

- `keyword`
- `authorId`
- `authorEmail`
- `visibility`
- `isDeleted`
- `createdFrom`
- `createdTo`

### 처리 규칙

- 관리자 삭제도 우선 `soft delete`
- 삭제 사유를 요청 바디 또는 쿼리로 받아 감사 로그에 저장

### 삭제 요청 예시

```json
{
  "reason": "운영 정책 위반 게시물"
}
```

현재 `Post.softDelete()`가 존재하므로 재사용성이 높다.

---

## 7.4 댓글 관리

### 주요 기능

- 댓글 검색/조회
- 게시물별 댓글 모니터링
- 강제 삭제

### 권장 API

- `GET /api/admin/comments`
- `GET /api/admin/comments/{commentId}`
- `DELETE /api/admin/comments/{commentId}`

### 구현 포인트

- 일반 댓글 삭제 로직이 이미 있으므로 관리자 전용 서비스 메서드만 추가해도 된다.
- "작성자 본인만 삭제 가능" 검증을 관리자 경로에서는 우회할 수 있어야 한다.
- 삭제 이력은 반드시 `AdminAuditLog`에 남긴다.

---

## 7.5 신고 관리

이 기능은 2차 구현으로 권장한다.

### 주요 기능

- 신고 접수
- 신고 목록 조회
- 대상별 누적 신고 수 조회
- 신고 승인/반려
- 신고 처리 후 제재 연결

### 권장 API

- `POST /api/reports`
- `GET /api/admin/reports`
- `GET /api/admin/reports/{reportId}`
- `PATCH /api/admin/reports/{reportId}/status`
- `POST /api/admin/reports/{reportId}/resolve`

### 신고 대상

- 사용자
- 게시물
- 댓글

---

## 8. DTO 설계 제안

1차 구현 기준으로 아래 DTO가 있으면 충분하다.

### Dashboard

- `AdminDashboardSummaryResponse`

### User

- `AdminUserListResponse`
- `AdminUserDetailResponse`
- `AdminUserStatusUpdateRequest`
- `AdminUserRoleUpdateRequest`

### Post

- `AdminPostListResponse`
- `AdminPostDetailResponse`
- `AdminDeleteRequest`

### Comment

- `AdminCommentListResponse`
- `AdminCommentDetailResponse`

### Audit

- `AdminAuditLogResponse`

DTO는 기존 `ApiResponse<T>` 래퍼를 그대로 재사용하면 된다.

---

## 9. Repository 확장 제안

관리자 기능은 검색과 필터링이 핵심이므로 JpaSpecificationExecutor 도입을 권장한다.

### 권장 변경

- `UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>`
- `PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post>`
- 댓글도 필요 시 동일 적용

### 이유

- 상태/권한/기간/키워드 필터 조합이 많다
- 메서드명 기반 쿼리만으로는 유지보수가 빠르게 어려워진다

초기에는 간단히 `Pageable + 조건 몇 개`로 시작해도 되지만, 관리자 화면이 커질수록 Specification 또는 Querydsl이 더 적합하다.

---

## 10. 서비스 계층 설계 제안

관리자 기능은 일반 서비스에 섞기보다 별도 서비스로 분리하는 것이 좋다.

### 권장 클래스

- `AdminDashboardService`
- `AdminUserService`
- `AdminPostService`
- `AdminCommentService`
- `AdminAuditLogService`

### 분리 이유

- 일반 사용자 로직과 운영 로직의 정책이 다름
- 권한 체크가 더 복잡함
- 감사 로그 기록이 거의 모든 관리자 액션에 필요함

---

## 11. 컨트롤러 구조 제안

### 권장 패키지

- `controller.admin`
- `dto.admin.dashboard`
- `dto.admin.user`
- `dto.admin.post`
- `dto.admin.comment`
- `service.admin`

### 권장 컨트롤러

- `AdminDashboardController`
- `AdminUserController`
- `AdminPostController`
- `AdminCommentController`
- `AdminReportController`

---

## 12. 감사 로그 정책

관리자 기능에서 가장 중요한 운영 안전장치는 감사 로그다.

### 반드시 로그를 남겨야 하는 액션

- 사용자 상태 변경
- 사용자 권한 변경
- 게시물 삭제
- 댓글 삭제
- 신고 처리

### 저장해야 할 정보

- 수행 관리자 ID
- 수행 시각
- 작업 종류
- 대상 타입
- 대상 ID
- 사유
- 변경 전 상태
- 변경 후 상태

### 구현 팁

서비스 메서드 안에서 비즈니스 처리 후 직접 저장해도 되지만, 공통화하려면 `AdminAuditLogService.record(...)` 형태로 추상화하는 것이 좋다.

---

## 13. 보안 및 운영 주의사항

### 13.1 절대 필요한 보안 규칙

- 관리자 API는 모두 JWT 인증 필수
- `ROLE_ADMIN` 체크 필수
- 중요 액션은 `isSuperUser` 추가 검증
- 자기 자신 강등/정지 금지

### 13.2 응답 데이터 최소화

관리자 목록 API에서도 비밀번호, refresh token, 내부 보안 민감값은 절대 노출하지 않는다.

### 13.3 운영 위험 요소

현재 `application.yaml`에서 `spring.jpa.hibernate.ddl-auto: create`로 설정되어 있다.
관리자 기능 추가 이전에 개발/운영 프로파일 분리를 더 엄격히 하고, 운영 환경에서는 `validate` 또는 `none`으로 전환하는 것이 안전하다.

### 13.4 인코딩 주의

현재 일부 소스/문서에서 한글 주석이 깨져 보이는 부분이 있다.
관리자 문서/코드 추가 시 UTF-8 인코딩을 일관되게 유지하는 것이 좋다.

---

## 14. 권장 개발 순서

### Phase 1. 관리자 접근 기반

- `SecurityConfig`에 `/api/admin/**` 권한 규칙 추가
- 관리자 컨트롤러 패키지 생성
- 관리자 여부 공통 체크 유틸 또는 서비스 작성

### Phase 2. 대시보드/사용자 관리

- 대시보드 요약 API 구현
- 사용자 목록/상세/상태 변경 구현
- 감사 로그 엔티티 및 저장 로직 추가

### Phase 3. 게시물/댓글 관리

- 관리자 게시물 목록/상세/삭제
- 관리자 댓글 목록/상세/삭제
- 검색/필터 API 추가

### Phase 4. 신고/제재 고도화

- 신고 엔티티/리포지토리/서비스/컨트롤러
- 신고 승인 후 자동 제재 연동
- 정지 기간 및 제재 이력 관리

---

## 15. 테스트 전략

관리자 기능은 권한/운영 사고와 직결되므로 일반 API보다 테스트 우선순위가 높다.

### 반드시 필요한 테스트

- 일반 사용자의 관리자 API 접근 차단
- 관리자 API 접근 허용
- 사용자 상태 변경 성공/실패 케이스
- 본인 계정 강등/정지 금지
- 게시물 강제 삭제 후 soft delete 반영
- 감사 로그 생성 여부

### 테스트 유형

- Controller: Security + HTTP status 검증
- Service: 상태 전이 및 감사 로그 검증
- Repository: 필터링 조회 검증

---

## 16. 구현 시 바로 재사용 가능한 기존 자산

현재 프로젝트에서 아래 자산은 그대로 재사용 가능하다.

- `ApiResponse<T>` 공통 응답 구조
- `User.role`, `User.status`, `isSuperUser`
- `Post.isDeleted` 기반 soft delete
- `GlobalExceptionHandler`
- JWT 인증 구조
- 페이지네이션 패턴(`Pageable`, `PageResponse`)

즉, 관리자 기능은 프로젝트를 뒤엎는 작업이 아니라 "운영 레이어를 추가하는 작업"으로 접근하는 것이 맞다.

---

## 17. 최종 권장안

현재 프로젝트 기준으로 가장 현실적인 1차 목표는 아래다.

1. `/api/admin/**` 보안 경로 추가
2. 관리자 대시보드 요약 API 추가
3. 사용자 관리 API 추가
4. 게시물/댓글 강제 삭제 API 추가
5. `AdminAuditLog` 추가

이 5가지만 구현해도 운영 가능한 최소 관리자 백엔드는 갖춰진다.

그 다음 단계에서 신고/제재 자동화를 붙이는 방식이 가장 안전하다.

---

## 18. 다음 구현 후보

이 문서 다음 단계로 바로 이어서 작업한다면 구현 우선순위는 아래를 추천한다.

1. `AdminAuditLog` 엔티티/리포지토리 추가
2. `SecurityConfig`에 관리자 경로 보호 추가
3. `AdminUserController`, `AdminUserService` 추가
4. `AdminDashboardController` 추가
5. `AdminPostController`, `AdminCommentController` 추가

이 순서로 가면 권한, 추적성, 운영 기능을 균형 있게 확보할 수 있다.
