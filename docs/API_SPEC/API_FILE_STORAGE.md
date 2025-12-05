# 파일 저장소 API 명세서

## 개요

파일 업로드, 삭제 및 URL 조회를 위한 API입니다.

---

## 1. 파일 업로드

### `POST /files/upload`

파일을 서버에 업로드합니다.

**인증**: 불필요  
**Content-Type**: `multipart/form-data`

### Request

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `file` | File | ✅ | 업로드할 파일 |

### 요청 예시 (cURL)

```bash
curl -X POST http://localhost:8080/files/upload \
  -F "file=@/path/to/image.jpg"
```

### 요청 예시 (Postman)

1. **Method**: POST
2. **URL**: `http://localhost:8080/files/upload`
3. **Body**: form-data
4. **Key**: `file` (Type: File)
5. **Value**: 파일 선택

### 성공 응답 (200 OK)

```
https://storage.example.com/uploads/abc123-image.jpg
```

- 업로드된 파일의 접근 가능한 URL이 반환됩니다.

---

## 2. 파일 삭제 (파일명)

### `DELETE /files/delete`

파일명으로 파일을 삭제합니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `fileName` | String | ✅ | 삭제할 파일명 |

### 요청 예시

```
DELETE /files/delete?fileName=abc123-image.jpg
```

### 성공 응답 (200 OK)

```
Deleted: abc123-image.jpg
```

---

## 3. 파일 삭제 (URL)

### `DELETE /files/delete-by-url`

파일 URL로 파일을 삭제합니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `fileUrl` | String | ✅ | 삭제할 파일의 전체 URL |

### 요청 예시

```
DELETE /files/delete-by-url?fileUrl=https://storage.example.com/uploads/abc123-image.jpg
```

### 성공 응답 (200 OK)

```
Deleted: https://storage.example.com/uploads/abc123-image.jpg
```

---

## 4. 파일 URL 조회

### `GET /files/url`

파일명으로 파일의 접근 URL을 조회합니다.

**인증**: 불필요

### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `fileName` | String | ✅ | 조회할 파일명 |

### 요청 예시

```
GET /files/url?fileName=abc123-image.jpg
```

### 성공 응답 (200 OK)

```
https://storage.example.com/uploads/abc123-image.jpg
```

---

## cURL 예시

### 파일 업로드
```bash
curl -X POST http://localhost:8080/files/upload \
  -F "file=@/path/to/profile.jpg"
```

### 파일 삭제 (파일명)
```bash
curl -X DELETE "http://localhost:8080/files/delete?fileName=abc123-image.jpg"
```

### 파일 삭제 (URL)
```bash
curl -X DELETE "http://localhost:8080/files/delete-by-url?fileUrl=https://storage.example.com/uploads/abc123-image.jpg"
```

### 파일 URL 조회
```bash
curl -X GET "http://localhost:8080/files/url?fileName=abc123-image.jpg"
```

---

## 지원 파일 형식

업로드 가능한 파일 형식은 서버 설정에 따라 다를 수 있습니다.

### 일반적으로 지원되는 이미지 형식
- JPEG (.jpg, .jpeg)
- PNG (.png)
- GIF (.gif)
- WebP (.webp)

---

## 주의사항

1. 업로드된 파일명은 중복 방지를 위해 고유 식별자가 추가됩니다.
2. 파일 삭제 시 복구가 불가능하므로 주의가 필요합니다.
3. 대용량 파일 업로드 시 서버 설정에 따라 제한이 있을 수 있습니다.

