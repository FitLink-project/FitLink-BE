# 시설 API 명세서

## 개요

공공체육시설 검색, 조회 및 경로 안내 API입니다.

---

## 1. 시설 통합 검색

### `GET /api/facility`

키워드로 체육시설을 검색합니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `keyword` | String | ✅ | 검색 키워드 (시설명, 주소 등) |

### 요청 예시

```
GET /api/facility?keyword=강남체육관
```

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": [
    {
      "facilityId": 1,
      "facilityName": "강남종합체육관",
      "address": "서울특별시 강남구 삼성동 123-45",
      "latitude": 37.5089,
      "longitude": 127.0634
    },
    {
      "facilityId": 2,
      "facilityName": "강남구민체육센터",
      "address": "서울특별시 강남구 역삼동 678-90",
      "latitude": 37.5001,
      "longitude": 127.0365
    }
  ]
}
```

---

## 2. 주변 시설 조회

### `POST /api/facility/nearby`

현재 위치 기준 반경 내의 체육시설을 조회합니다.

**인증**: 불필요  
**Content-Type**: `application/json`

### Request Body

```json
{
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `latitude` | Double | ✅ | 현재 위치 위도 |
| `longitude` | Double | ✅ | 현재 위치 경도 |

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": [
    {
      "facilityId": 1,
      "facilityName": "종로구민체육센터",
      "address": "서울특별시 종로구 세종로 100",
      "latitude": 37.5700,
      "longitude": 126.9800,
      "distance": 523.5
    },
    {
      "facilityId": 2,
      "facilityName": "광화문체육관",
      "address": "서울특별시 종로구 광화문로 50",
      "latitude": 37.5720,
      "longitude": 126.9765,
      "distance": 780.2
    }
  ]
}
```

### 응답 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `facilityId` | Long | 시설 고유 ID |
| `facilityName` | String | 시설명 |
| `address` | String | 주소 |
| `latitude` | Double | 위도 |
| `longitude` | Double | 경도 |
| `distance` | Double | 현재 위치와의 거리 (미터) |

---

## 3. 시설 상세 조회

### `GET /api/facility/{facilityId}`

특정 시설의 상세 정보를 조회합니다.

**인증**: 불필요

### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `facilityId` | Long | ✅ | 시설 ID |

### 요청 예시

```
GET /api/facility/1
```

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "facilityId": 1,
    "facilityName": "강남종합체육관",
    "address": "서울특별시 강남구 삼성동 123-45",
    "latitude": 37.5089,
    "longitude": 127.0634,
    "programNames": ["수영", "헬스"],
    "homepageUrl": "https://gangnam-sports.or.kr"
  }
}
```

### 응답 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `facilityId` | Long | 시설 고유 ID |
| `facilityName` | String | 시설명 |
| `address` | String | 주소 |
| `latitude` | Double | 위도 |
| `longitude` | Double | 경도 |
| `programNames` | List<String> | 대표 프로그램 목록 (최대 2개) |
| `homepageUrl` | String | 시설 홈페이지 URL |

---

## 4. 시설 프로그램 조회

### `GET /api/facility/{facilityId}/programs`

특정 시설에서 운영하는 프로그램 목록을 조회합니다.

**인증**: 불필요

### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `facilityId` | Long | ✅ | 시설 ID |

### 요청 예시

```
GET /api/facility/1/programs
```

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "facilityId": 1,
    "facilityName": "강남종합체육관",
    "address": "서울특별시 강남구 삼성동 123-45",
    "homepage": "https://gangnam-sports.or.kr",
    "programs": [
      {
        "programId": 1,
        "name": "수영교실 A반",
        "target": "성인",
        "days": "월, 수, 금",
        "time": "10:00-12:00",
        "capacity": 30,
        "price": 50000
      },
      {
        "programId": 2,
        "name": "헬스",
        "target": "전연령",
        "days": "월, 화, 수, 목, 금",
        "time": "06:00-22:00",
        "capacity": 100,
        "price": 45000
      },
      {
        "programId": 3,
        "name": "요가",
        "target": "성인여성",
        "days": "화, 목",
        "time": "14:00-15:30",
        "capacity": 20,
        "price": 40000
      }
    ]
  }
}
```

### 프로그램 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `programId` | Long | 프로그램 고유 ID |
| `name` | String | 프로그램명 |
| `target` | String | 대상 (성인, 어린이, 전연령 등) |
| `days` | String | 운영 요일 |
| `time` | String | 운영 시간 |
| `capacity` | Integer | 정원 |
| `price` | Integer | 가격 (원) |

---

## 5. 경로 조회

### `GET /api/facility/route`

출발지에서 목적지까지의 경로를 조회합니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `originLat` | Float | ✅ | 출발지 위도 |
| `originLng` | Float | ✅ | 출발지 경도 |
| `destLat` | Float | ✅ | 목적지 위도 |
| `destLng` | Float | ✅ | 목적지 경도 |
| `type` | String | ✅ | 이동 수단 (`car` / `walk` / `transit`) |

### 요청 예시

```
GET /api/facility/route?originLat=37.5665&originLng=126.9780&destLat=37.5089&destLng=127.0634&type=car
```

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "type": "car",
    "distance": 8500,
    "duration": 1200,
    "path": [
      [37.5665, 126.9780],
      [37.5600, 126.9850],
      [37.5400, 127.0200],
      [37.5089, 127.0634]
    ],
    "waypoints": [
      {
        "lat": 37.5665,
        "lng": 126.9780,
        "description": "출발: 시청역"
      },
      {
        "lat": 37.5400,
        "lng": 127.0200,
        "description": "경유: 잠실역"
      },
      {
        "lat": 37.5089,
        "lng": 127.0634,
        "description": "도착: 강남종합체육관"
      }
    ]
  }
}
```

### 응답 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `type` | String | 이동 수단 |
| `distance` | Integer | 총 거리 (미터) |
| `duration` | Integer | 예상 소요 시간 (초) |
| `path` | List<List<Double>> | 경로 좌표 목록 [위도, 경도] |
| `waypoints` | List<Waypoint> | 주요 경유지 정보 |

### 이동 수단 타입

| 타입 | 설명 |
|------|------|
| `car` | 자동차 경로 |
| `walk` | 도보 경로 |
| `transit` | 대중교통 경로 |

---

## cURL 예시

### 시설 검색
```bash
curl -X GET "http://localhost:8080/api/facility?keyword=체육관"
```

### 주변 시설 조회
```bash
curl -X POST http://localhost:8080/api/facility/nearby \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 37.5665,
    "longitude": 126.9780
  }'
```

### 시설 상세 조회
```bash
curl -X GET http://localhost:8080/api/facility/1
```

### 프로그램 조회
```bash
curl -X GET http://localhost:8080/api/facility/1/programs
```

### 경로 조회
```bash
curl -X GET "http://localhost:8080/api/facility/route?originLat=37.5665&originLng=126.9780&destLat=37.5089&destLng=127.0634&type=car"
```

