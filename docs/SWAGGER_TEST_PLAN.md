# planwith_fo_comment 기능별 Swagger 테스트 계획표

## 공통 테스트 값

```text
STORY_UUID: aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa
STORY_OWNER_UUID: bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb
AUTHOR_UUID: cccccccc-cccc-cccc-cccc-cccccccccccc
REPLY_AUTHOR_UUID: dddddddd-dddd-dddd-dddd-dddddddddddd
OTHER_UUID: eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee
ADMIN_UUID: ffffffff-ffff-ffff-ffff-ffffffffffff
COMMENT_UUID: 일반 댓글 생성 응답의 commentUuid 값
REPLY_UUID: 대댓글 생성 응답의 commentUuid 값
FLAT_REPLY_UUID: 대댓글에 답글 작성 응답의 commentUuid 값
```

응답의 `<생성일시>`, `<수정일시>`, `<오류발생일시>` 및 생성되는 UUID는 실행 시점의 실제 값으로 확인한다.

> Swagger UI에서 `<COMMENT_UUID>`, `<REPLY_UUID>` 등을 문자 그대로 전송하면 UUID 변환 실패로 HTTP 400이 발생한다. 반드시 직전 생성 API 응답의 실제 `commentUuid`로 교체한다.

## 사전 데이터 준비

Comment 서비스는 Kafka로 전달된 Story 및 Member 정보를 로컬 Projection에 저장한다. Kafka Consumer를 사용하지 않는 단독 Swagger 테스트에서는 애플리케이션 실행 후 아래 데이터를 Comment DB에 준비한다.

```sql
INSERT INTO comment_story_projection (
    story_uuid,
    owner_member_uuid,
    comment_enabled,
    story_status,
    source_version,
    synchronized_at
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    1,
    'ACTIVE',
    1,
    UTC_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE
    owner_member_uuid = VALUES(owner_member_uuid),
    comment_enabled = VALUES(comment_enabled),
    story_status = VALUES(story_status),
    source_version = VALUES(source_version),
    synchronized_at = VALUES(synchronized_at);

INSERT INTO comment_member_projection (
    member_uuid,
    nickname,
    profile_image,
    member_status,
    source_version,
    synchronized_at
) VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccccc', '댓글작성자', 'https://images.example.com/author.png', 'ACTIVE', 1, UTC_TIMESTAMP(6)),
    ('dddddddd-dddd-dddd-dddd-dddddddddddd', '답글작성자', 'https://images.example.com/reply.png', 'ACTIVE', 1, UTC_TIMESTAMP(6)),
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '다른회원', 'https://images.example.com/other.png', 'ACTIVE', 1, UTC_TIMESTAMP(6))
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname),
    profile_image = VALUES(profile_image),
    member_status = VALUES(member_status),
    source_version = VALUES(source_version),
    synchronized_at = VALUES(synchronized_at);
```

---

X-MEMBER-UUID: 없음

기능명: 배포 상태 확인
api 명: GET /api/planwith-fo-comment/deploy-check

Request body:
```text
없음
```

Response body:
```json
{
  "service": "planwith-fo-comment",
  "marker": "planwith-fo-comment-deploy-v1",
  "message": "planwith-fo-comment deploy pipeline ok"
}
```

확인 사항: HTTP 200인지 확인한다.

---

X-MEMBER-UUID: 없음

기능명: 로그인
api 명: POST /api/planwith-fo-comment/login

Request body:
```json
{
  "id": "test-001",
  "pw": "1234"
}
```

Response body:
```json
{
  "id": "test-001",
  "message": "로그인에 성공했습니다."
}
```

확인 사항: HTTP 200인지 확인한다.

---

X-MEMBER-UUID: 없음

기능명: 비회원 댓글 작성 실패
api 명: POST /api/planwith-fo-comment/comments

Request body:
```json
{
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "parentCommentUuid": null,
  "commentContent": "비회원 댓글 작성 테스트"
}
```

Response body:
```json
{
  "timestamp": "<오류발생일시>",
  "status": 401,
  "code": "LOGIN_REQUIRED",
  "message": "<로그인 필요 메시지>"
}
```

확인 사항: HTTP 401이고 `code=LOGIN_REQUIRED`인지 확인한다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: 일반 댓글 작성
api 명: POST /api/planwith-fo-comment/comments

Request body:
```json
{
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "parentCommentUuid": null,
  "commentContent": "좋은 여행 이야기네요!"
}
```

Response body:
```json
{
  "commentUuid": "<COMMENT_UUID>",
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "memberUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "parentCommentUuid": null,
  "profileImage": "https://images.example.com/author.png",
  "nickname": "댓글작성자",
  "memberStatus": "ACTIVE",
  "commentContent": "좋은 여행 이야기네요!",
  "likeCount": 0,
  "reportCount": 0,
  "storyOwnerMemberUuid": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "commentEnabled": true,
  "storyStatus": "ACTIVE",
  "createdAt": "<생성일시>",
  "updatedAt": "<수정일시>",
  "isUpdated": false,
  "canEdit": true,
  "canDelete": true
}
```

확인 사항: HTTP 201인지 확인하고 응답의 `commentUuid`를 이후 테스트의 `COMMENT_UUID`로 사용한다.

---

X-MEMBER-UUID: dddddddd-dddd-dddd-dddd-dddddddddddd

기능명: 대댓글 작성
api 명: POST /api/planwith-fo-comment/comments

Request body:
```json
{
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "parentCommentUuid": "<COMMENT_UUID>",
  "commentContent": "저도 가보고 싶어요."
}
```

Response body:
```json
{
  "commentUuid": "<REPLY_UUID>",
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "memberUuid": "dddddddd-dddd-dddd-dddd-dddddddddddd",
  "parentCommentUuid": "<COMMENT_UUID>",
  "profileImage": "https://images.example.com/reply.png",
  "nickname": "답글작성자",
  "memberStatus": "ACTIVE",
  "commentContent": "저도 가보고 싶어요.",
  "likeCount": 0,
  "reportCount": 0,
  "storyOwnerMemberUuid": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "commentEnabled": true,
  "storyStatus": "ACTIVE",
  "createdAt": "<생성일시>",
  "updatedAt": "<수정일시>",
  "isUpdated": false,
  "canEdit": true,
  "canDelete": true
}
```

확인 사항: HTTP 201인지 확인하고 응답의 `commentUuid`를 `REPLY_UUID`로 사용한다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: 대댓글에 답글 작성 및 2단계 평면화
api 명: POST /api/planwith-fo-comment/comments

Request body:
```json
{
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "parentCommentUuid": "<REPLY_UUID>",
  "commentContent": "@답글작성자 댓글 감사합니다!"
}
```

Response body:
```json
{
  "commentUuid": "<FLAT_REPLY_UUID>",
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "memberUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "parentCommentUuid": "<COMMENT_UUID>",
  "profileImage": "https://images.example.com/author.png",
  "nickname": "댓글작성자",
  "memberStatus": "ACTIVE",
  "commentContent": "@답글작성자 댓글 감사합니다!",
  "likeCount": 0,
  "reportCount": 0,
  "storyOwnerMemberUuid": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "commentEnabled": true,
  "storyStatus": "ACTIVE",
  "createdAt": "<생성일시>",
  "updatedAt": "<수정일시>",
  "isUpdated": false,
  "canEdit": true,
  "canDelete": true
}
```

확인 사항: HTTP 201이고, 요청은 `REPLY_UUID`를 답글 대상으로 전달했지만 응답의 `parentCommentUuid`는 최상위 `COMMENT_UUID`로 정규화되는지 확인한다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: Story별 댓글 목록 최신순 조회
api 명: GET /api/planwith-fo-comment/stories/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/comments?sort=LATEST

Request body:
```text
없음
```

Response body:
```json
[
  {
    "commentUuid": "<COMMENT_UUID>",
    "parentCommentUuid": null,
    "member": {
      "memberUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
      "nickname": "댓글작성자",
      "profileImage": "https://images.example.com/author.png"
    },
    "commentContent": "좋은 여행 이야기네요!",
    "commentLikeCount": 0,
    "createdAt": "<생성일시>",
    "updatedAt": "<수정일시>",
    "isUpdated": false,
    "canEdit": true,
    "canDelete": true,
    "isDeleted": false,
    "replies": [
      {
        "commentUuid": "<REPLY_UUID>",
        "parentCommentUuid": "<COMMENT_UUID>",
        "member": {
          "memberUuid": "dddddddd-dddd-dddd-dddd-dddddddddddd",
          "nickname": "답글작성자",
          "profileImage": "https://images.example.com/reply.png"
        },
        "commentContent": "저도 가보고 싶어요.",
        "commentLikeCount": 0,
        "createdAt": "<생성일시>",
        "updatedAt": "<수정일시>",
        "isUpdated": false,
        "canEdit": false,
        "canDelete": false,
        "isDeleted": false,
        "replies": []
      },
      {
        "commentUuid": "<FLAT_REPLY_UUID>",
        "parentCommentUuid": "<COMMENT_UUID>",
        "member": {
          "memberUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
          "nickname": "댓글작성자",
          "profileImage": "https://images.example.com/author.png"
        },
        "commentContent": "@답글작성자 댓글 감사합니다!",
        "commentLikeCount": 0,
        "createdAt": "<생성일시>",
        "updatedAt": "<수정일시>",
        "isUpdated": false,
        "canEdit": true,
        "canDelete": true,
        "isDeleted": false,
        "replies": []
      }
    ]
  }
]
```

확인 사항: HTTP 200이며 일반 댓글의 `replies`에 두 답글이 같은 깊이로 포함되고, 각 답글의 `replies`는 빈 목록인지 확인한다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: Story별 댓글 목록 좋아요순 조회
api 명: GET /api/planwith-fo-comment/stories/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/comments?sort=LIKE

Request body:
```text
없음
```

Response body:
```json
[
  {
    "commentUuid": "<COMMENT_UUID>",
    "parentCommentUuid": null,
    "commentContent": "좋은 여행 이야기네요!",
    "commentLikeCount": 0,
    "replies": [
      {
        "commentUuid": "<REPLY_UUID>",
        "parentCommentUuid": "<COMMENT_UUID>",
        "commentContent": "저도 가보고 싶어요.",
        "commentLikeCount": 0,
        "replies": []
      },
      {
        "commentUuid": "<FLAT_REPLY_UUID>",
        "parentCommentUuid": "<COMMENT_UUID>",
        "commentContent": "@답글작성자 댓글 감사합니다!",
        "commentLikeCount": 0,
        "replies": []
      }
    ]
  }
]
```

확인 사항: HTTP 200인지 확인한다. 좋아요 수 변경은 Kafka 이벤트가 필요하므로 단독 Swagger에서는 정렬 파라미터 처리까지만 검증한다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: 댓글 상세 조회
api 명: GET /api/planwith-fo-comment/comments/{COMMENT_UUID}

Request body:
```text
없음
```

Response body:
```json
{
  "commentUuid": "<COMMENT_UUID>",
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "memberUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "parentCommentUuid": null,
  "profileImage": "https://images.example.com/author.png",
  "nickname": "댓글작성자",
  "memberStatus": "ACTIVE",
  "commentContent": "좋은 여행 이야기네요!",
  "likeCount": 0,
  "reportCount": 0,
  "storyOwnerMemberUuid": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "commentEnabled": true,
  "storyStatus": "ACTIVE",
  "createdAt": "<생성일시>",
  "updatedAt": "<수정일시>",
  "isUpdated": false,
  "canEdit": true,
  "canDelete": true
}
```

확인 사항: HTTP 200이며 `canEdit=true`, `canDelete=true`인지 확인한다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: 댓글 수정
api 명: PATCH /api/planwith-fo-comment/comments/{COMMENT_UUID}

Request body:
```json
{
  "commentContent": "수정된 댓글 내용입니다."
}
```

Response body:
```json
{
  "commentUuid": "<COMMENT_UUID>",
  "storyUuid": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "memberUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
  "parentCommentUuid": null,
  "commentContent": "수정된 댓글 내용입니다.",
  "createdAt": "<생성일시>",
  "updatedAt": "<수정일시>",
  "isUpdated": true,
  "canEdit": true,
  "canDelete": true
}
```

확인 사항: HTTP 200이며 `commentContent`가 변경되고 `isUpdated=true`인지 확인한다.

---

X-MEMBER-UUID: eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee

기능명: 타 회원 댓글 수정 실패
api 명: PATCH /api/planwith-fo-comment/comments/{COMMENT_UUID}

Request body:
```json
{
  "commentContent": "다른 회원이 수정한 내용"
}
```

Response body:
```json
{
  "timestamp": "<오류발생일시>",
  "status": 403,
  "code": "COMMENT_OWNER_MISMATCH",
  "message": "<댓글 작성자 불일치 메시지>"
}
```

확인 사항: HTTP 403이고 `code=COMMENT_OWNER_MISMATCH`인지 확인한다.

---

X-MEMBER-UUID: eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee

기능명: 타 회원 댓글 삭제 실패
api 명: DELETE /api/planwith-fo-comment/comments/{COMMENT_UUID}

Request body:
```text
없음
```

Response body:
```json
{
  "timestamp": "<오류발생일시>",
  "status": 403,
  "code": "COMMENT_DELETE_FORBIDDEN",
  "message": "<댓글 삭제 권한 없음 메시지>"
}
```

확인 사항: HTTP 403이고 `code=COMMENT_DELETE_FORBIDDEN`인지 확인한다.

---

X-MEMBER-UUID: 없음

기능명: 숨김 댓글 관리 비회원 조회 실패
api 명: GET /api/planwith-fo-comment/stories/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/comments/management

Request body:
```text
없음
```

Response body:
```json
{
  "timestamp": "<오류발생일시>",
  "status": 401,
  "code": "LOGIN_REQUIRED",
  "message": "<로그인 필요 메시지>"
}
```

확인 사항: HTTP 401이고 `code=LOGIN_REQUIRED`인지 확인한다.

---

X-MEMBER-UUID: bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb

기능명: Story 작성자 숨김 댓글 관리 조회
api 명: GET /api/planwith-fo-comment/stories/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/comments/management

Request body:
```text
없음
```

Response body:
```json
[]
```

확인 사항: HTTP 200인지 확인한다. 신고 이벤트로 숨김 처리된 댓글이 없다면 빈 배열이 정상이다.

---

X-MEMBER-UUID: ffffffff-ffff-ffff-ffff-ffffffffffff
X-MEMBER-ROLE: ADMIN

기능명: ADMIN 숨김 댓글 관리 조회
api 명: GET /api/planwith-fo-comment/stories/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/comments/management

Request body:
```text
없음
```

Response body:
```json
[]
```

확인 사항: HTTP 200인지 확인한다. 실제 숨김 댓글이 있으면 `commentUuid`, `profileImage`, `nickname`, `commentContent`, `reportCount`, `createdAt`, `hiddenAt`이 신고 횟수 내림차순으로 반환된다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: 댓글 작성자 댓글 삭제
api 명: DELETE /api/planwith-fo-comment/comments/{COMMENT_UUID}

Request body:
```text
없음
```

Response body:
```text
없음 (HTTP 204)
```

확인 사항: HTTP 204인지 확인한다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: 삭제된 부모 댓글과 대댓글 목록 조회
api 명: GET /api/planwith-fo-comment/stories/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa/comments?sort=LATEST

Request body:
```text
없음
```

Response body:
```json
[
  {
    "commentUuid": "<COMMENT_UUID>",
    "parentCommentUuid": null,
    "member": {
      "memberUuid": "cccccccc-cccc-cccc-cccc-cccccccccccc",
      "nickname": "댓글작성자",
      "profileImage": "https://images.example.com/author.png"
    },
    "commentContent": "삭제된 댓글입니다.",
    "commentLikeCount": 0,
    "createdAt": "<생성일시>",
    "updatedAt": "<삭제일시>",
    "isUpdated": false,
    "canEdit": false,
    "canDelete": false,
    "isDeleted": true,
    "replies": [
      {
        "commentUuid": "<REPLY_UUID>",
        "parentCommentUuid": "<COMMENT_UUID>",
        "commentContent": "저도 가보고 싶어요.",
        "isDeleted": false,
        "replies": []
      },
      {
        "commentUuid": "<FLAT_REPLY_UUID>",
        "parentCommentUuid": "<COMMENT_UUID>",
        "commentContent": "@답글작성자 댓글 감사합니다!",
        "isDeleted": false,
        "replies": []
      }
    ]
  }
]
```

확인 사항: 삭제된 부모 댓글은 대체 문구로 표시되고 `REPLY_UUID`, `FLAT_REPLY_UUID`가 모두 같은 `replies` 배열에 유지되는지 확인한다.

---

X-MEMBER-UUID: cccccccc-cccc-cccc-cccc-cccccccccccc

기능명: 삭제된 댓글 상세 조회 실패
api 명: GET /api/planwith-fo-comment/comments/{COMMENT_UUID}

Request body:
```text
없음
```

Response body:
```json
{
  "timestamp": "<오류발생일시>",
  "status": 404,
  "code": "COMMENT_NOT_FOUND",
  "message": "<댓글을 찾을 수 없음 메시지>"
}
```

확인 사항: HTTP 404이고 `code=COMMENT_NOT_FOUND`인지 확인한다.

## 기능 실행 순서

1. Projection 사전 데이터 준비
2. 배포 상태 확인 및 로그인
3. 비회원 댓글 작성 실패 확인
4. 일반 댓글 생성 후 `COMMENT_UUID` 저장
5. 대댓글 생성 후 `REPLY_UUID` 저장
6. 대댓글에 답글 작성 후 최상위 댓글 아래로 평면화되는지 확인
7. 댓글 목록 최신순·좋아요순 조회
8. 댓글 상세 조회 및 작성자 권한 확인
9. 댓글 수정 및 타 회원 수정 차단 확인
10. 타 회원 삭제 차단 확인
11. 숨김 댓글 관리 API 권한 확인
12. 작성자 댓글 삭제
13. 삭제된 부모 댓글과 대댓글 유지 확인
14. 삭제 댓글 상세 404 확인

## 단독 Swagger 테스트 제한사항

- Story 및 Member Projection은 원래 Kafka 이벤트로 동기화되므로, 실제 통합 테스트에서는 SQL 준비 대신 Kafka 이벤트를 사용해야 한다.
- 댓글 좋아요 수는 Like 서비스의 Kafka 이벤트로 갱신된다. Comment Swagger에는 좋아요 변경 API가 없다.
- 댓글 신고 수와 자동 숨김은 Report 서비스의 Kafka 이벤트로 갱신된다. Comment Swagger에는 신고 API가 없다.
- 숨김 댓글 관리 API에서 실제 데이터를 확인하려면 동일 댓글에 신고 이벤트가 3회 이상 반영되어야 한다.
- `X-Member-Uuid`, `X-Member-Role`은 Gateway가 인증 후 주입하는 신뢰 헤더이며 운영 환경에서 클라이언트가 임의로 전달하면 안 된다.
