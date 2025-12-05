# FitLink API 종합 명세서

## 📋 목차

1. [개요](#개요)
2. [인증](#인증)
3. [공통 응답 형식](#공통-응답-형식)
4. [API 목록](#api-목록)
   - [사용자 API](#1-사용자-api)
   - [체력 측정 API](#2-체력-측정-api)
   - [AI 운동 처방 API](#3-ai-운동-처방-api)
   - [시설 API](#4-시설-api)
   - [지도 API](#5-지도-api)
   - [동영상 API](#6-동영상-api)
   - [파일 API](#7-파일-api)
   - [헬스체크 API](#8-헬스체크-api)

---

## 개요

FitLink 백엔드 API 서버입니다.

- **Base URL**: `{server_url}/api`
- **인증 방식**: JWT Bearer Token
- **Content-Type**: `application/json` (기본) / `multipart/form-data` (파일 업로드)

---

## 인증

### JWT 토큰 인증
대부분의 API는 JWT 토큰 인증이 필요합니다.

**헤더 형식:**
```
Authorization: Bearer {access_token}
```

### 인증이 필요 없는 API
- `POST /api/user/join` - 회원가입
- `POST /api/user/login` - 로그인
- `GET /api/health` - 헬스체크
- `GET /login` - 로그인 페이지

---

## 공통 응답 형식

### 성공 응답
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": { ... }
}
```

### 실패 응답
```json
{
  "isSuccess": false,
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "result": null
}
```

### 주요 에러 코드
| 코드 | 설명 |
|------|------|
| `COMMON200` | 성공 |
| `COMMON400` | 잘못된 요청 |
| `COMMON500` | 서버 에러 |
| `USER4001` | 이메일 형식 오류 |
| `USER4002` | 비밀번호 형식 오류 |
| `USER4031` | 중복된 이메일 |
| `NOT_FOUND` | 리소스를 찾을 수 없음 |

---

## API 목록

---

# 1. 사용자 API

## 1.1 회원가입

### `POST /api/user/join`

**인증**: 불필요  
**Content-Type**: `multipart/form-data`

#### Request
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `name` | String | ✅ | 사용자 이름 |
| `email` | String | ✅ | 이메일 주소 |
| `password` | String | ✅ | 비밀번호 (8자 이상, 영문+숫자) |
| `agreements` | JSON | ✅ | 약관 동의 정보 |
| `agreements.privacy` | Boolean | ✅ | 개인정보 처리방침 동의 |
| `agreements.service` | Boolean | ✅ | 서비스 이용약관 동의 |
| `agreements.over14` | Boolean | ✅ | 만 14세 이상 확인 |
| `agreements.location` | Boolean | ❌ | 위치 정보 이용 동의 |
| `Img` | File | ❌ | 프로필 이미지 파일 |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "userId": 1,
    "createdAt": "2025-01-15T14:30:00"
  }
}
```

---

## 1.2 로그인

### `POST /api/user/login`

**인증**: 불필요  
**Content-Type**: `application/json`

#### Request Body
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `email` | String | ✅ | 이메일 주소 |
| `password` | String | ✅ | 비밀번호 |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "userId": 1,
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

## 1.3 프로필 조회

### `GET /api/user/profile`

**인증**: ✅ 필요 (Bearer Token)

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "userId": 1,
    "email": "test@example.com",
    "name": "홍길동",
    "profileUrl": "https://storage.example.com/profile.jpg",
    "isActive": true,
    "regDate": "2025-01-15T14:30:00",
    "provider": "LOCAL",
    "deleteDate": null,
    "agreements": {
      "privacy": true,
      "service": true,
      "over14": true,
      "location": false
    },
    "height": 175.0,
    "weight": 70.5,
    "birthDate": "19900101",
    "sex": "M"
  }
}
```

---

## 1.4 프로필 수정

### `PATCH /api/user/edit`

**인증**: ✅ 필요 (Bearer Token)  
**Content-Type**: `multipart/form-data`

#### Request
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `name` | String | ❌ | 사용자 이름 |
| `email` | String | ❌ | 이메일 주소 |
| `password` | String | ❌ | 비밀번호 |
| `agreements` | JSON | ❌ | 약관 동의 정보 |
| `Img` | File | ❌ | 프로필 이미지 파일 |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "userId": 1,
    "email": "updated@example.com",
    "name": "김철수",
    ...
  }
}
```

---

## 1.5 이메일 수정

### `PATCH /api/user/email`

**인증**: ✅ 필요 (Bearer Token)  
**Content-Type**: `application/json`

#### Request Body
```json
{
  "email": "newemail@example.com"
}
```

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "userId": 1,
    "createdAt": "2025-01-15T14:30:00"
  }
}
```

---

## 1.6 회원 탈퇴 (소프트 삭제)

### `DELETE /api/user/delete`

**인증**: ✅ 필요 (Bearer Token)

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "userId": 1,
    "email": "test@example.com",
    "isActive": false,
    "regDate": "2025-01-15T14:30:00",
    "provider": "LOCAL",
    "deleteDate": "2025-02-01T10:00:00"
  }
}
```

---

## 1.7 회원 완전 삭제 (하드 삭제)

### `DELETE /api/user/delete/hard`

**인증**: ✅ 필요 (Bearer Token)

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "userId": 1,
    "email": "test@example.com",
    "isActive": false,
    "regDate": "2025-01-15T14:30:00",
    "provider": "LOCAL",
    "deleteDate": "2025-02-01T10:00:00"
  }
}
```

---

# 2. 체력 측정 API

## 2.1 국민체력100 측정 결과 저장

### `POST /api/fitness/kookmin`

**인증**: ✅ 필요 (Bearer Token)  
**Content-Type**: `application/json`

#### Request Body
```json
{
  "sex": "M",
  "birthDate": "19900101",
  "height": 175.0,
  "weight": 70.5,
  "gripStrength": 45.5,
  "sitUp": 30,
  "crossSitUp": 25,
  "sitAndReach": 15.2,
  "shuttleRun": 50,
  "sprint": 12.5,
  "standingLongJump": 220.0
}
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `sex` | String | ✅ | 성별 ("M" / "F") |
| `birthDate` | String | ✅ | 생년월일 (YYYYMMDD) |
| `height` | Float | ✅ | 키 (cm) |
| `weight` | Float | ✅ | 체중 (kg) |
| `gripStrength` | Float | ✅ | 악력 (kg) |
| `sitUp` | Integer | ✅ | 윗몸말아올리기 (회) |
| `crossSitUp` | Integer | ❌ | 교차윗몸일으키기 (회) |
| `sitAndReach` | Float | ✅ | 앉아 윗몸 앞으로 굽히기 (cm) |
| `shuttleRun` | Integer | ✅ | 20m 왕복 오래달리기 (회) |
| `sprint` | Float | ✅ | 10m 왕복 달리기 (초) |
| `standingLongJump` | Float | ✅ | 제자리 멀리뛰기 (cm) |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "strength": 85.0,
    "muscular": 78.0,
    "flexibility": 72.0,
    "cardiopulmonary": 80.0,
    "agility": 88.0,
    "quickness": 82.0,
    "standard": {
      "grade1": {
        "gripStrength": 48.0,
        "sitUp": 35.0,
        "sitAndReach": 18.0,
        "shuttleRun": 60.0,
        "sprint": 11.0,
        "standingLongJump": 240.0
      },
      "grade2": {
        "gripStrength": 42.0,
        "sitUp": 28.0,
        "sitAndReach": 12.0,
        "shuttleRun": 45.0,
        "sprint": 13.5,
        "standingLongJump": 200.0
      }
    },
    "userInfo": {
      "sex": "M",
      "birthDate": "19900101",
      "height": 175.0,
      "weight": 70.5
    },
    "testKookmin": {
      "gripStrength": 45.5,
      "sitUp": 30,
      "sitAndReach": 15.2,
      "shuttleRun": 50,
      "sprint": 12.5,
      "standingLongJump": 220.0
    }
  }
}
```

---

## 2.2 국민체력100 측정 결과 수정

### `PATCH /api/fitness/kookmin`

**인증**: ✅ 필요 (Bearer Token)  
**Content-Type**: `application/json`

> 기존 측정 결과를 업데이트합니다. Request Body는 `POST /api/fitness/kookmin`과 동일합니다.

---

## 2.3 간단 체력 측정 결과 저장

### `POST /api/fitness/general`

**인증**: ✅ 필요 (Bearer Token)  
**Content-Type**: `application/json`

#### Request Body
```json
{
  "sex": "M",
  "birthDate": "19900101",
  "height": 175.0,
  "weight": 70.5,
  "sliderStrength": 80,
  "sitUp": 25,
  "sitAndReach": 12.5,
  "ymcaStepTest": 42.0,
  "sliderAgility": 75,
  "sliderPower": 85
}
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `sex` | String | ✅ | 성별 ("M" / "F") |
| `birthDate` | String | ✅ | 생년월일 (YYYYMMDD) |
| `height` | Float | ✅ | 키 (cm) |
| `weight` | Float | ✅ | 체중 (kg) |
| `sliderStrength` | Integer | ✅ | 근력 슬라이더 값 |
| `sitUp` | Integer | ✅ | 윗몸일으키기 (회) |
| `sitAndReach` | Float | ✅ | 앉아 윗몸 앞으로 굽히기 (cm) |
| `ymcaStepTest` | Float | ✅ | YMCA 스텝 테스트 결과 |
| `sliderAgility` | Integer | ✅ | 민첩성 슬라이더 값 |
| `sliderPower` | Integer | ✅ | 순발력 슬라이더 값 |

#### Response
> `POST /api/fitness/kookmin`과 유사한 형식, `testKookmin` 대신 `testGeneral` 포함

---

## 2.4 간단 체력 측정 결과 수정

### `PATCH /api/fitness/general`

**인증**: ✅ 필요 (Bearer Token)  
**Content-Type**: `application/json`

> 기존 측정 결과를 업데이트합니다. Request Body는 `POST /api/fitness/general`과 동일합니다.

---

## 2.5 체력 측정 결과 조회

### `GET /api/fitness/result`

**인증**: ✅ 필요 (Bearer Token)

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "strength": 85.0,
    "muscular": 78.0,
    "flexibility": 72.0,
    "cardiopulmonary": 80.0,
    "agility": 88.0,
    "quickness": 82.0,
    "standard": { ... },
    "userInfo": { ... },
    "testKookmin": { ... }
  }
}
```

---

# 3. AI 운동 처방 API

## 3.1 AI 기반 운동 처방 생성

### `POST /api/ai/prescription`

**인증**: ✅ 필요 (Bearer Token)  
**Content-Type**: `application/json`

#### Request Body
```json
{
  "age": 30,
  "gender": 1,
  "height": 175,
  "weight": 70
}
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `age` | Integer | ✅ | 나이 |
| `gender` | Integer | ✅ | 성별 (0: 여자, 1: 남자) |
| `height` | Integer | ✅ | 키 (cm) |
| `weight` | Integer | ✅ | 몸무게 (kg) |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "warmup": ["스트레칭", "조깅"],
    "mainExercise": ["스쿼트", "런지", "플랭크"],
    "cooldown": ["정적 스트레칭", "심호흡"]
  }
}
```

---

# 4. 시설 API

## 4.1 시설 통합 검색

### `GET /api/facility`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `keyword` | String | ✅ | 검색 키워드 |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": [
    {
      "facilityId": 1,
      "facilityName": "종합체육관",
      "address": "서울특별시 강남구...",
      "latitude": 37.5665,
      "longitude": 126.9780
    }
  ]
}
```

---

## 4.2 주변 시설 조회

### `POST /api/facility/nearby`

**인증**: 필요 없음  
**Content-Type**: `application/json`

#### Request Body
```json
{
  "latitude": 37.5665,
  "longitude": 126.9780
}
```

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `latitude` | Double | ✅ | 위도 |
| `longitude` | Double | ✅ | 경도 |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": [
    {
      "facilityId": 1,
      "facilityName": "강남체육관",
      "address": "서울특별시 강남구...",
      "latitude": 37.5665,
      "longitude": 126.9780,
      "distance": 500.5
    }
  ]
}
```

---

## 4.3 시설 상세 조회

### `GET /api/facility/{facilityId}`

**인증**: 필요 없음

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `facilityId` | Long | ✅ | 시설 ID |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "facilityId": 1,
    "facilityName": "강남종합체육관",
    "address": "서울특별시 강남구...",
    "latitude": 37.5665,
    "longitude": 126.9780,
    "programNames": ["수영", "헬스"],
    "homepageUrl": "https://example.com"
  }
}
```

---

## 4.4 시설 프로그램 조회

### `GET /api/facility/{facilityId}/programs`

**인증**: 필요 없음

#### Path Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `facilityId` | Long | ✅ | 시설 ID |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "facilityId": 1,
    "facilityName": "강남종합체육관",
    "address": "서울특별시 강남구...",
    "homepage": "https://example.com",
    "programs": [
      {
        "programId": 1,
        "name": "수영교실",
        "target": "성인",
        "days": "월, 수, 금",
        "time": "10:00-12:00",
        "capacity": 30,
        "price": 50000
      }
    ]
  }
}
```

---

## 4.5 경로 조회

### `GET /api/facility/route`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `originLat` | Float | ✅ | 출발지 위도 |
| `originLng` | Float | ✅ | 출발지 경도 |
| `destLat` | Float | ✅ | 목적지 위도 |
| `destLng` | Float | ✅ | 목적지 경도 |
| `type` | String | ✅ | 이동 수단 (car/walk/transit) |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "type": "car",
    "distance": 5000,
    "duration": 900,
    "path": [[37.5665, 126.9780], [37.5700, 126.9800]],
    "waypoints": [
      {
        "lat": 37.5665,
        "lng": 126.9780,
        "description": "출발"
      }
    ]
  }
}
```

---

# 5. 지도 API

## 5.1 역지오코딩 (좌표 → 주소)

### `GET /api/maps/reverse`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `lat` | Double | ✅ | 위도 |
| `lon` | Double | ✅ | 경도 |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "address": "서울특별시 강남구 삼성동 123-45"
  }
}
```

---

## 5.2 POI 검색 (테스트용)

### `GET /api/test/tmap`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `keyword` | String | ✅ | 검색 키워드 |

---

# 6. 동영상 API

## 6.1 국민체력100 동영상 목록 조회

### `GET /api/video`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|----------|------|------|--------|------|
| `pageNo` | Integer | ❌ | 1 | 페이지 번호 |
| `numOfRows` | Integer | ❌ | 10 | 한 페이지당 결과 수 |
| `fitnessFactor` | String | ✅ | - | 체력 요소 키워드 |

#### Response
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "response": {
      "body": {
        "totalCount": 100,
        "items": [...]
      }
    }
  }
}
```

---

## 6.2 동영상 스트리밍

### `GET /api/video/stream`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `url` | String | ✅ | 동영상 URL |

#### Response
- **Content-Type**: `video/mp4`
- 동영상 바이너리 스트림 반환

---

# 7. 파일 API

## 7.1 파일 업로드

### `POST /files/upload`

**인증**: 필요 없음  
**Content-Type**: `multipart/form-data`

#### Request
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `file` | File | ✅ | 업로드할 파일 |

#### Response
```
https://storage.example.com/uploaded-file-name.jpg
```

---

## 7.2 파일 삭제

### `DELETE /files/delete`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `fileName` | String | ✅ | 삭제할 파일명 |

#### Response
```
Deleted: filename.jpg
```

---

## 7.3 파일 URL로 삭제

### `DELETE /files/delete-by-url`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `fileUrl` | String | ✅ | 삭제할 파일 URL |

#### Response
```
Deleted: https://storage.example.com/file.jpg
```

---

## 7.4 파일 URL 조회

### `GET /files/url`

**인증**: 필요 없음

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `fileName` | String | ✅ | 파일명 |

#### Response
```
https://storage.example.com/filename.jpg
```

---

# 8. 헬스체크 API

## 8.1 서버 상태 확인

### `GET /api/health`

**인증**: 불필요

#### Response
```json
{
  "status": "UP",
  "timestamp": "2025-01-15T14:30:00+09:00",
  "service": "fitlink-be"
}
```

---

# 9. 소셜 로그인 (OAuth2)

## 9.1 로그인 페이지

### `GET /login`

**인증**: 불필요

HTML 로그인 페이지를 반환합니다. Google, Kakao 소셜 로그인 버튼 포함.

---

## 9.2 OAuth2 인증

### Google 로그인
```
GET /oauth2/authorization/google
```

### Kakao 로그인
```
GET /oauth2/authorization/kakao
```

> OAuth2 인증 완료 후 프론트엔드로 리다이렉트되며, JWT 토큰이 전달됩니다.

---

## API 엔드포인트 요약표

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/user/join` | 회원가입 | ❌ |
| `POST` | `/api/user/login` | 로그인 | ❌ |
| `GET` | `/api/user/profile` | 프로필 조회 | ✅ |
| `PATCH` | `/api/user/edit` | 프로필 수정 | ✅ |
| `PATCH` | `/api/user/email` | 이메일 수정 | ✅ |
| `DELETE` | `/api/user/delete` | 회원 탈퇴 (소프트) | ✅ |
| `DELETE` | `/api/user/delete/hard` | 회원 완전 삭제 | ✅ |
| `POST` | `/api/fitness/kookmin` | 국민체력100 저장 | ✅ |
| `PATCH` | `/api/fitness/kookmin` | 국민체력100 수정 | ✅ |
| `POST` | `/api/fitness/general` | 간단체력 저장 | ✅ |
| `PATCH` | `/api/fitness/general` | 간단체력 수정 | ✅ |
| `GET` | `/api/fitness/result` | 체력 결과 조회 | ✅ |
| `POST` | `/api/ai/prescription` | AI 운동 처방 | ✅ |
| `GET` | `/api/facility` | 시설 검색 | ❌ |
| `POST` | `/api/facility/nearby` | 주변 시설 조회 | ❌ |
| `GET` | `/api/facility/{id}` | 시설 상세 조회 | ❌ |
| `GET` | `/api/facility/{id}/programs` | 시설 프로그램 조회 | ❌ |
| `GET` | `/api/facility/route` | 경로 조회 | ❌ |
| `GET` | `/api/maps/reverse` | 역지오코딩 | ❌ |
| `GET` | `/api/video` | 동영상 목록 | ❌ |
| `GET` | `/api/video/stream` | 동영상 스트리밍 | ❌ |
| `POST` | `/files/upload` | 파일 업로드 | ❌ |
| `DELETE` | `/files/delete` | 파일 삭제 | ❌ |
| `DELETE` | `/files/delete-by-url` | URL로 파일 삭제 | ❌ |
| `GET` | `/files/url` | 파일 URL 조회 | ❌ |
| `GET` | `/api/health` | 헬스체크 | ❌ |

---

*최종 업데이트: 2025-12-05*

