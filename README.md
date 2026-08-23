# planwith_fo_comment

서버 노트북 Self-hosted Runner 배포 확인용 Spring Boot 서비스입니다.

| 항목 | 값 |
| --- | --- |
| Compose / Eureka 이름 | `planwith-fo-comment` |
| 이미지 | `planwith/planwith-fo-comment:latest` |
| 포트 | `8090` |
| 배포 확인 | `GET /api/planwith-fo-comment/deploy-check` |

## 로컬 실행

```powershell
.\gradlew.bat bootRun
```

- Swagger UI (로컬 bootRun만): `http://localhost:8090/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8090/v3/api-docs`
- Swagger UI (다른 PC / Docker): `http://localhost:8000/swagger-ui.html` (select `planwith-fo-comment`)
- Deploy check (서버 localhost): `http://localhost:8090/api/planwith-fo-comment/deploy-check`

다른 PC는 서비스 포트 `8090`를 직접 호출하지 않는다. 경로는 `브라우저 → Gateway :8000 → Eureka → planwith-fo-comment:8090` 이다. OpenAPI `servers`는 `GATEWAY_PUBLIC_URL=/` 이라 Swagger가 Docker hostname을 쓰지 않는다.

## 로그인 테스트

```json
{
  "id": "test-001",
  "pw": "1234"
}
```

| 환경 변수 | 기본값 |
| --- | --- |
| `LOGIN_ID` | `test-001` |
| `LOGIN_PASSWORD` | `1234` |
| `DEPLOY_MARKER` | `planwith-fo-comment-deploy-v1` |

## 서버 배포 확인

1. GitHub 레포 생성 후 push (`develop` 또는 `main`)
2. `planwith-infra` compose에 `planwith-fo-comment` 등록 후 서버 `C:\planwith\docker-compose.yml` 반영
3. Actions Deploy 성공 확인
4. `http://<서버IP>:8000/swagger-ui.html` 에서 `planwith-fo-comment` 선택 후 API 호출
5. (서버 PC localhost) `http://127.0.0.1:8090/api/planwith-fo-comment/deploy-check` 응답의 `marker` 확인
