# 지도 API 명세서

## 개요

Tmap 기반 지도 관련 API입니다. 역지오코딩(좌표 → 주소 변환) 및 POI 검색 기능을 제공합니다.

---

## 1. 역지오코딩 (좌표 → 주소)

### `GET /api/maps/reverse`

위도/경도 좌표를 주소로 변환합니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `lat` | Double | ✅ | 위도 |
| `lon` | Double | ✅ | 경도 |

### 요청 예시

```
GET /api/maps/reverse?lat=37.5665&lon=126.9780
```

### 성공 응답 (200 OK)

```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공했습니다.",
  "result": {
    "fullAddress": "서울특별시 중구 세종대로 110",
    "roadAddress": "세종대로 110",
    "jibunAddress": "태평로1가 31",
    "city": "서울특별시",
    "county": "중구",
    "dong": "태평로1가"
  }
}
```

### 응답 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `fullAddress` | String | 전체 주소 |
| `roadAddress` | String | 도로명 주소 |
| `jibunAddress` | String | 지번 주소 |
| `city` | String | 시/도 |
| `county` | String | 구/군 |
| `dong` | String | 동/읍/면 |

---

## 2. POI 검색 (테스트용)

### `GET /api/test/tmap`

키워드로 POI(관심 지점)를 검색합니다.

> ⚠️ 이 API는 테스트 목적으로 제공됩니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `keyword` | String | ✅ | 검색 키워드 |

### 요청 예시

```
GET /api/test/tmap?keyword=강남역
```

### 성공 응답 (200 OK)

```json
{
  "searchPoiInfo": {
    "totalCount": 100,
    "count": 10,
    "pois": {
      "poi": [
        {
          "id": "123456",
          "name": "강남역",
          "telNo": "02-1234-5678",
          "frontLat": 37.4979,
          "frontLon": 127.0276,
          "noorLat": 37.4979,
          "noorLon": 127.0276,
          "upperAddrName": "서울특별시",
          "middleAddrName": "강남구",
          "lowerAddrName": "역삼동",
          "detailAddrName": "858"
        }
      ]
    }
  }
}
```

### POI 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | String | POI 고유 ID |
| `name` | String | POI 이름 |
| `telNo` | String | 전화번호 |
| `frontLat` | Double | 정면 입구 위도 |
| `frontLon` | Double | 정면 입구 경도 |
| `noorLat` | Double | 중심점 위도 |
| `noorLon` | Double | 중심점 경도 |
| `upperAddrName` | String | 시/도 |
| `middleAddrName` | String | 구/군 |
| `lowerAddrName` | String | 동/읍/면 |
| `detailAddrName` | String | 상세 주소 |

---

## cURL 예시

### 역지오코딩
```bash
curl -X GET "http://localhost:8080/api/maps/reverse?lat=37.5665&lon=126.9780"
```

### POI 검색
```bash
curl -X GET "http://localhost:8080/api/test/tmap?keyword=강남역"
```

---

## 사용 예시

### JavaScript에서 현재 위치 주소 조회

```javascript
// 현재 위치 가져오기
navigator.geolocation.getCurrentPosition(async (position) => {
  const { latitude, longitude } = position.coords;
  
  // 주소 조회
  const response = await fetch(
    `/api/maps/reverse?lat=${latitude}&lon=${longitude}`
  );
  const data = await response.json();
  
  if (data.isSuccess) {
    console.log('현재 위치:', data.result.fullAddress);
  }
});
```

