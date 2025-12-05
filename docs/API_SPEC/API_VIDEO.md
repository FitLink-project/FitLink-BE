# 동영상 API 명세서

## 개요

국민체력100 운동 동영상 조회 및 스트리밍 API입니다.

---

## 1. 동영상 목록 조회

### `GET /api/video`

국민체력100 운동 동영상 목록을 조회합니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `pageNo` | Integer | ❌ | 1 | 페이지 번호 |
| `numOfRows` | Integer | ❌ | 10 | 한 페이지당 결과 수 |
| `fitnessFactor` | String | ✅ | - | 체력 요소 키워드 |

### 체력 요소 (fitnessFactor) 예시

| 키워드 | 설명 |
|--------|------|
| 근력 | 근력 관련 운동 동영상 |
| 유연성 | 유연성 관련 운동 동영상 |
| 심폐지구력 | 심폐지구력 관련 운동 동영상 |
| 민첩성 | 민첩성 관련 운동 동영상 |
| 순발력 | 순발력 관련 운동 동영상 |

### 요청 예시

```
GET /api/video?fitnessFactor=근력&pageNo=1&numOfRows=10
```

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "response": {
      "header": {
        "resultCode": "00",
        "resultMsg": "NORMAL SERVICE."
      },
      "body": {
        "totalCount": 150,
        "pageNo": 1,
        "numOfRows": 10,
        "items": {
          "item": [
            {
              "videoNo": 1,
              "videoTitle": "악력 강화 운동",
              "videoUrl": "https://example.com/video/grip-strength.mp4",
              "thumbnailUrl": "https://example.com/thumb/grip-strength.jpg",
              "fitnessFactor": "근력",
              "duration": "05:30",
              "description": "악력을 강화하는 효과적인 운동 방법"
            },
            {
              "videoNo": 2,
              "videoTitle": "팔굽혀펴기 자세 교정",
              "videoUrl": "https://example.com/video/pushup.mp4",
              "thumbnailUrl": "https://example.com/thumb/pushup.jpg",
              "fitnessFactor": "근력",
              "duration": "08:15",
              "description": "올바른 팔굽혀펴기 자세와 변형 동작"
            }
          ]
        }
      }
    }
  }
}
```

### 응답 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `totalCount` | Integer | 전체 동영상 수 |
| `pageNo` | Integer | 현재 페이지 번호 |
| `numOfRows` | Integer | 페이지당 결과 수 |
| `items.item` | Array | 동영상 목록 |

### 동영상 아이템 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `videoNo` | Integer | 동영상 고유 번호 |
| `videoTitle` | String | 동영상 제목 |
| `videoUrl` | String | 동영상 URL |
| `thumbnailUrl` | String | 썸네일 이미지 URL |
| `fitnessFactor` | String | 체력 요소 분류 |
| `duration` | String | 동영상 길이 (MM:SS) |
| `description` | String | 동영상 설명 |

---

## 2. 동영상 스트리밍

### `GET /api/video/stream`

동영상을 프록시 스트리밍으로 제공합니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `url` | String | ✅ | 스트리밍할 동영상 URL |

### 요청 예시

```
GET /api/video/stream?url=https://example.com/video/exercise.mp4
```

### 응답

- **Content-Type**: `video/mp4`
- **Content-Disposition**: `inline`
- 동영상 바이너리 데이터 스트림

### 특징

- **프록시 스트리밍**: 원본 서버의 동영상을 중계하여 제공
- **리다이렉트 처리**: 301, 302, 307, 308 리다이렉트 자동 처리
- **브라우저 호환**: 브라우저에서 직접 재생 가능한 형식으로 제공

### 에러 응답

| HTTP 상태 | 설명 |
|-----------|------|
| 404 | 원본 서버에서 영상을 찾을 수 없음 |
| 500 | 원본 서버에서 영상을 가져올 수 없음 |

---

## cURL 예시

### 동영상 목록 조회
```bash
curl -X GET "http://localhost:8080/api/video?fitnessFactor=근력&pageNo=1&numOfRows=10"
```

### 동영상 스트리밍
```bash
# 브라우저에서 직접 접속하거나 curl로 다운로드
curl -X GET "http://localhost:8080/api/video/stream?url=https://example.com/video/exercise.mp4" --output video.mp4
```

---

## 사용 예시

### 프론트엔드에서 동영상 재생

```html
<video controls>
  <source src="http://localhost:8080/api/video/stream?url=https://example.com/video/exercise.mp4" type="video/mp4">
</video>
```

### JavaScript에서 동영상 목록 가져오기

```javascript
const response = await fetch('/api/video?fitnessFactor=근력&pageNo=1&numOfRows=10');
const data = await response.json();

if (data.isSuccess) {
  const videos = data.result.response.body.items.item;
  videos.forEach(video => {
    console.log(video.videoTitle, video.videoUrl);
  });
}
```

