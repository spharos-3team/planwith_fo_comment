# Release Note / RM — planwith-fo-comment

> 기준 브랜치: `develop`  
> 기준 커밋: `62cef7d`  
> 애플리케이션 버전: `0.0.1-SNAPSHOT`  
> 기술 스택: Java 17, Spring Boot 4.0.7, Spring Data JPA, Kafka, MySQL

## 1. 서비스 개요

`planwith-fo-comment`는 Story에 등록되는 댓글과 대댓글을 관리하는 독립 MSA 서비스입니다.

주요 책임은 다음과 같습니다.

- 댓글 및 1단계 대댓글 작성·조회·수정·삭제
- 댓글 작성자, Story 작성자, ADMIN 권한 판정
- Like/Unlike 이벤트 기반 좋아요 수 Projection
- Report 이벤트 기반 신고 수 Projection
- 신고 3회 이상 댓글 자동 숨김
- Story 작성자 및 ADMIN용 숨김 댓글 관리
- Member/Story 이벤트 기반 로컬 Projection 관리
- 댓글 변경 이벤트 Outbox 저장 및 Kafka 발행
- Kafka 이벤트 중복·역순·이전 version 처리 방지
- 프론트 화면 제어용 `canEdit`, `canDelete` 응답 제공

서비스는 댓글 원장만 소유하며 회원, Story, 좋아요, 신고 원장은 각각의 외부 도메인 서비스가 소유합니다.

---

## 2. 도메인 범위

### 댓글 원장

| 항목 | 내용 |
|---|---|
| 테이블 | `story_comment` |
| 식별자 | 내부 `comment_id`, 외부 공개 `comment_uuid` |
| Story 연결 | `story_uuid` |
| 작성자 연결 | `member_uuid` |
| 대댓글 | `parent_comment_uuid`를 사용하는 1단계 구조 |
| 내용 제한 | 필수, 공백 불가, 최대 1,000자 |
| 삭제 | `deleted_at` 기반 Soft Delete |
| 노출 상태 | `VISIBLE`, `HIDDEN` |
| 자동 숨김 | `report_count >= 3` |
| 좋아요 | `comment_like_count` Projection |
| 신고 | `report_count` Projection |

### 댓글 정책

- 로그인 회원만 댓글과 대댓글을 작성할 수 있습니다.
- Story Projection이 존재하고 `commentEnabled=true`, `storyStatus=ACTIVE`인 경우에만 작성할 수 있습니다.
- 대댓글에는 추가 대댓글을 작성할 수 없습니다.
- 삭제되거나 숨김 처리된 댓글에는 대댓글을 작성할 수 없습니다.
- 댓글 수정은 댓글 작성자만 가능합니다.
- 댓글 삭제는 댓글 작성자, Story 작성자 또는 ADMIN만 가능합니다.
- 부모 댓글이 삭제되어도 활성 대댓글이 있으면 삭제 안내 문구와 대댓글을 유지합니다.
- 일반 목록은 삭제되지 않은 `VISIBLE` 댓글을 기준으로 조회합니다.
- 관리 목록은 삭제되지 않은 `HIDDEN` 댓글을 신고 횟수 내림차순으로 조회합니다.

### Projection 및 처리 이력

| 테이블 | 책임 |
|---|---|
| `comment_member_projection` | 닉네임, 프로필, 회원 상태, `source_version` |
| `comment_story_projection` | Story 작성자, 댓글 허용 여부, Story 상태, `source_version` |
| `comment_like_projection` | 처리된 Like 상태 및 좋아요 Counter 정합성 |
| `comment_report_projection` | 처리된 Report UUID 및 신고 Counter 정합성 |
| `processed_comment_event` | Kafka `event_uuid` 기반 중복 처리 방지 |
| `comment_outbox` | 댓글 생성·수정·삭제 이벤트 발행 대기 이력 |

---

## 3. API 그룹

### Comment Command API

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| `POST` | `/api/planwith-fo-comment/comments` | 로그인 회원 | 댓글 또는 대댓글 작성 |
| `PATCH` | `/api/planwith-fo-comment/comments/{commentUuid}` | 댓글 작성자 | 댓글 내용 수정 |
| `DELETE` | `/api/planwith-fo-comment/comments/{commentUuid}` | 작성자, Story 작성자, ADMIN | 댓글 Soft Delete |

댓글 생성 API는 `201 Created`와 전체 댓글 화면 모델을 즉시 반환하며 다음 헤더를 제공합니다.

```http
Location: /api/planwith-fo-comment/comments/{commentUuid}
```

### Comment Query API

| Method | URL | 권한 | 설명 |
|---|---|---|---|
| `GET` | `/api/planwith-fo-comment/comments/{commentUuid}` | 비회원 포함 | 댓글 상세 조회 |
| `GET` | `/api/planwith-fo-comment/stories/{storyUuid}/comments` | 비회원 포함 | Story 댓글 목록 조회 |
| `GET` | `/api/planwith-fo-comment/stories/{storyUuid}/comments/management` | Story 작성자, ADMIN | 숨김 댓글 관리 조회 |

목록 정렬 옵션은 다음과 같습니다.

- `LATEST`: 최신 댓글 우선
- `LIKE`: 좋아요 수 우선

응답에는 사용자별 권한 정보가 포함됩니다.

```json
{
  "canEdit": true,
  "canDelete": true
}
```

### 운영 확인 API

| Method | URL | 설명 |
|---|---|---|
| `GET` | `/api/planwith-fo-comment/deploy-check` | 배포 Marker 및 서비스 상태 확인 |
| `POST` | `/api/planwith-fo-comment/login` | 환경변수 기반 배포 확인용 로그인 |
| `GET` | `/actuator/health` | 서비스 Health Check |
| `GET` | `/v3/api-docs` | OpenAPI 문서 |
| `GET` | `/swagger-ui.html` | Swagger UI |

---

## 4. 외부 연동

### 외부 SaaS API

- 없음

### 내부 서비스 이벤트 수신

| 연동 서비스 | Kafka Topic 기본값 | 처리 내용 |
|---|---|---|
| Member Service | `member.changed` | 닉네임, 프로필, 회원 상태 Projection 갱신 |
| Story Service | `story.created` | Story Projection 생성 |
| Story Service | `story.updated` | 댓글 허용 여부 및 Story 정보 갱신 |
| Story Service | `story.deleted` | Story 삭제 상태 반영 |
| Like Service | `like.created` | 댓글 좋아요 수 증가 |
| Like Service | `like.removed` | 댓글 좋아요 수 감소 |
| Report Service | `report.created` | 신고 수 증가 및 자동 숨김 |

수신 이벤트 필수 메타데이터는 다음과 같습니다.

```text
eventUuid
eventType
targetUuid
occurredAt
```

Member/Story 이벤트에는 양수 `sourceVersion`이 추가로 필요합니다.

### Comment 이벤트 발행

| 이벤트 | Kafka Topic 기본값 |
|---|---|
| `COMMENT_CREATED` | `comment.created` |
| `COMMENT_UPDATED` | `comment.updated` |
| `COMMENT_DELETED` | `comment.deleted` |

댓글 상태 변경과 Outbox 저장은 동일 DB 트랜잭션에서 처리됩니다.

### Infrastructure

- MySQL
- Kafka
- Eureka Service Discovery
- API Gateway
- H2 테스트 데이터베이스

Redis와 SSE는 확장 포인트만 고려되어 있으며 현재 구현에는 포함되지 않습니다.

---

## 5. 비기능 / 품질

### 정합성

- 댓글 상태 변경과 Outbox 저장을 하나의 트랜잭션으로 처리
- `eventUuid` 기반 Kafka 중복 이벤트 차단
- Like 이벤트의 `occurredAt` 기반 역순 이벤트 차단
- Member/Story의 `sourceVersion` 기반 이전 이벤트 무시
- Counter 갱신 시 댓글 행 비관적 잠금 적용
- Like 및 Report UUID별 Projection으로 중복 Counter 반영 방지
- 삭제와 숨김 상태를 별도로 관리

### 데이터 접근

- `spring.jpa.open-in-view=false`
- Hibernate JDBC 시간대 UTC
- Story, 작성자, 부모 댓글, Moderation 조회용 인덱스 구성
- 관리 목록은 `report_count DESC`, `created_at DESC` 기준 조회
- Entity를 API 응답으로 직접 노출하지 않고 DTO를 사용

### 오류 처리

전역 예외 처리기를 통해 다음과 같은 일관된 오류 코드를 반환합니다.

- `LOGIN_REQUIRED`
- `STORY_NOT_FOUND`
- `STORY_DELETED`
- `COMMENT_NOT_ALLOWED`
- `COMMENT_NOT_FOUND`
- `COMMENT_OWNER_MISMATCH`
- `COMMENT_ALREADY_DELETED`
- `COMMENT_DELETE_FORBIDDEN`
- `COMMENT_MANAGEMENT_FORBIDDEN`
- `INVALID_REPLY`
- `INVALID_REQUEST`

### 관측 및 문서화

- Spring Boot Actuator Health/Info 제공
- Kubernetes Probe 호환 Health 설정
- OpenAPI 및 Swagger UI 제공
- 배포 Marker 확인 API 제공
- Java 컴파일에 `-Xlint:all` 적용

---

## 6. 배포 설정 요약

### Docker

- Build Image: `eclipse-temurin:17-jdk-alpine`
- Runtime Image: `eclipse-temurin:17-jre-alpine`
- 비루트 사용자 `spring`으로 실행
- 이미지 노출 포트: `8090`
- 실행 파일: Spring Boot Fat JAR

### 주요 환경변수

| 환경변수 | 기본값 | 설명 |
|---|---|---|
| `DB_URL` | 없음 | MySQL JDBC URL |
| `DB_USERNAME` | 없음 | DB 사용자 |
| `DB_PASSWORD` | 없음 | DB 비밀번호 |
| `JPA_DDL_AUTO` | `update` | Hibernate Schema 정책 |
| `SERVER_ADDRESS` | `0.0.0.0` | 서버 Bind 주소 |
| `SERVER_PORT` | `0` | 서버 포트 |
| `EUREKA_CLIENT_ENABLED` | `true` | Eureka 등록 여부 |
| `EUREKA_DEFAULT_ZONE` | `http://discovery:8761/eureka/` | Eureka 주소 |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka Broker |
| `KAFKA_CONSUMER_GROUP` | `planwith-fo-comment` | Consumer Group |
| `KAFKA_ENABLED` | `false` | Kafka Adapter 활성화 |
| `KAFKA_LISTENER_ENABLED` | `false` | Kafka Listener 시작 여부 |
| `OUTBOX_PUBLISHER_ENABLED` | `false` | Outbox Scheduler 활성화 |
| `OUTBOX_POLL_INTERVAL_MS` | `1000` | Outbox 조회 주기 |
| `GATEWAY_PUBLIC_URL` | `/` | OpenAPI Gateway URL |
| `SPRINGDOC_SWAGGER_UI_ENABLED` | `true` | Swagger UI 활성화 |
| `DEPLOY_MARKER` | `planwith-fo-comment-deploy-v1` | 배포 확인 Marker |

운영 배포 시 최소한 다음 값을 명시해야 합니다.

```text
SERVER_PORT=8090
DB_URL=jdbc:mysql://...
DB_USERNAME=...
DB_PASSWORD=...
KAFKA_BOOTSTRAP_SERVERS=...
KAFKA_ENABLED=true
KAFKA_LISTENER_ENABLED=true
OUTBOX_PUBLISHER_ENABLED=true
EUREKA_DEFAULT_ZONE=...
```

---

## 7. 운영 주의사항

1. `X-Member-Uuid`, `X-Member-Role` 헤더를 권한 판정에 사용하므로 외부에서 서비스를 직접 호출하지 않도록 해야 합니다. Gateway에서 인증된 값만 주입하고 클라이언트 입력 헤더를 제거하거나 덮어써야 합니다.

2. `/login`은 환경변수에 저장된 단일 ID/PW를 비교하는 배포 확인용 기능입니다. 실제 회원 인증 또는 JWT 발급 API로 사용하면 안 됩니다.

3. 기본 `SERVER_PORT`는 임의 포트인 `0`이지만 Dockerfile은 `8090`을 노출합니다. 운영 환경에서는 반드시 `SERVER_PORT=8090`을 지정해야 합니다.

4. Kafka, Kafka Listener, Outbox Publisher는 기본적으로 모두 비활성화되어 있습니다. 세 설정 중 일부만 활성화하면 이벤트 소비 또는 발행이 수행되지 않습니다.

5. Outbox Publisher는 `KafkaTemplate.send()` 완료 응답을 기다리지 않고 Outbox를 `PUBLISHED`로 변경합니다. 운영 안정성을 높이려면 Kafka Broker ACK 성공 이후 상태를 변경하도록 보완해야 합니다.

6. 다중 인스턴스가 동일한 Pending Outbox를 동시에 조회할 수 있으므로 Outbox Claim, 상태 선점 또는 `SKIP LOCKED` 정책 검토가 필요합니다.

7. 현재 발행되는 `CommentChangedEvent` payload에는 별도 `eventUuid`가 포함되지 않습니다. 다운스트림 서비스의 멱등 처리가 필요하면 `outboxUuid`를 이벤트 식별자로 전달하도록 계약을 확장해야 합니다.

8. `processed_comment_event`와 `comment_outbox`는 지속적으로 증가할 수 있습니다. Kafka 재처리 가능 기간과 장애 복구 정책에 맞는 보관 및 삭제 배치가 필요합니다.

9. 운영 Schema가 `ddl-auto=update`에 의존합니다. 운영 배포 전 Flyway 또는 Liquibase 기반 명시적 Migration 적용을 권장합니다.

10. 실제 Kafka Broker, MySQL, Eureka, Gateway를 포함한 인프라 E2E 테스트는 별도로 수행해야 합니다.

11. Redis 댓글 캐시와 SSE 실시간 전파는 구현되지 않았습니다. 현재 UX는 댓글 생성 REST 응답을 즉시 목록에 추가하는 방식입니다.

---

## 8. 개발 완료 범위 (단계 요약)

| 단계 | 완료 내용 | 상태 |
|---|---|---|
| 01 | Comment Domain 및 DB 구조 | 완료 |
| 02 | Member/Story Projection 및 Kafka Event 구조 | 완료 |
| 03 | 로그인, Story 상태, 내용 길이 Validation | 완료 |
| 04 | 댓글 및 1단계 대댓글 작성 | 완료 |
| 05 | 최신순·좋아요순 댓글 조회와 Projection 조합 | 완료 |
| 06 | 작성자 댓글 수정 및 수정 여부 표시 | 완료 |
| 07 | 작성자·Story 작성자·ADMIN Soft Delete | 완료 |
| 08 | Like/Unlike 이벤트 및 Counter Projection | 완료 |
| 09 | Report 이벤트 및 Counter Projection | 완료 |
| 10 | 신고 3회 자동 숨김 | 완료 |
| 11 | Story 작성자·ADMIN 숨김 댓글 관리 | 완료 |
| 12 | 비회원·작성자별 `canEdit`, `canDelete` 응답 | 완료 |
| 13 | Kafka 멱등성, 순서 및 Projection version 검증 | 완료 |
| 14 | 생성 댓글 즉시 REST 응답 및 Optimistic UI 지원 | 완료 |
| 15 | 전체 이벤트 스토밍 통합 시나리오 테스트 | 완료 |

---

## 9. 검증 상태

### 자동화 테스트

```text
./gradlew clean test --warning-mode all
BUILD SUCCESSFUL
```

| 항목 | 결과 |
|---|---|
| 전체 테스트 | 62개 |
| 성공 | 62개 |
| 실패 | 0개 |
| 오류 | 0개 |
| Java 컴파일 경고 | 0개 |
| 테스트 DB | H2 MySQL 호환 모드 |
| 테스트 Kafka | Application Input Port 기반 |
| REST API 테스트 | MockMvc 기반 |

통합 테스트는 다음 전체 흐름을 검증합니다.

```text
작성
→ 조회
→ 최신순/좋아요순 정렬
→ 대댓글
→ 수정 권한
→ 삭제 권한
→ Like/Unlike
→ Report
→ 신고 3회 자동 숨김
→ 일반 목록 제외
→ 관리 목록 조회 및 정렬
→ 관리자 삭제
→ Kafka 중복 이벤트 차단
→ Member Projection 갱신
```

실제 Kafka Broker 및 MySQL을 포함한 운영 인프라 E2E 검증은 자동화 범위에 포함되지 않습니다.

---

**RM 결론:** 댓글 서비스의 계획된 15단계 기능과 애플리케이션 통합 테스트는 완료되었습니다. 다만 운영 배포 전 인증 헤더 신뢰 경계, Kafka ACK 기반 Outbox 완료 처리, Outbox 다중 인스턴스 선점, 명시적 DB Migration 및 실제 Kafka/MySQL E2E 검증을 완료하는 조건으로 배포 승인하는 것이 적절합니다.
```
