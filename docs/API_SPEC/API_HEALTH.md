# 헬스체크 API 명세서

## 개요

서버 상태를 확인하기 위한 헬스체크 API입니다.

---

## 1. 서버 상태 확인

### `GET /api/health`

서버의 동작 상태를 확인합니다.

**인증**: 불필요

### 요청 예시

```
GET /api/health
```

### 성공 응답 (200 OK)

```json
{
  "status": "UP",
  "timestamp": "2025-12-05T14:30:00.123+09:00",
  "service": "fitlink-be"
}
```

### 응답 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `status` | String | 서버 상태 (`UP` / `DOWN`) |
| `timestamp` | String | 응답 시간 (ISO 8601 형식) |
| `service` | String | 서비스 이름 |

---

## 사용 목적

### 1. 로드밸런서 헬스체크
- AWS ALB, Nginx 등에서 백엔드 서버 상태 확인

### 2. 모니터링
- 서버 가동 상태 모니터링
- 다운타임 감지

### 3. 배포 확인
- 새 버전 배포 후 서버 정상 동작 확인

---

## cURL 예시

```bash
curl -X GET http://localhost:8080/api/health
```

---

## 모니터링 설정 예시

### AWS Target Group 설정
- **Protocol**: HTTP
- **Path**: `/api/health`
- **Port**: 8080
- **Healthy threshold**: 2
- **Unhealthy threshold**: 3
- **Timeout**: 5 seconds
- **Interval**: 30 seconds

### Docker Compose 헬스체크
```yaml
services:
  fitlink-be:
    image: fitlink-be:latest
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

### Kubernetes Liveness Probe
```yaml
livenessProbe:
  httpGet:
    path: /api/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  timeoutSeconds: 5
  failureThreshold: 3
```

