# 이미지 저장 및 서빙 설정 가이드

## 문제 상황

배포 환경에서 이미지 접근이 안 되는 경우, 다음을 확인해야 합니다:

1. **파일 저장 경로** (`file.dir`)
2. **블록 스토리지 마운트 경로**
3. **nginx 설정**

## 현재 설정

### application.properties (원격용)
```properties
file.dir=/app/images
file.url=/images/
file.base-url=https://www.fitlink1207.store
```

### 블록 스토리지 마운트
- 마운트 경로: `/data/images`

## 해결 방법

### 방법 1: file.dir을 블록 스토리지 경로로 변경 (권장)

`application.properties`에서 `file.dir`을 블록 스토리지 마운트 경로로 변경:

```properties
file.dir=/data/images
file.url=/images/
file.base-url=https://www.fitlink1207.store
```

**장점**: 
- 파일이 블록 스토리지에 직접 저장됨
- nginx 설정과 일치

### 방법 2: 심볼릭 링크 사용

`/app/images`를 `/data/images`로 심볼릭 링크:

```bash
# /app/images 디렉토리 삭제 (있는 경우)
sudo rm -rf /app/images

# 심볼릭 링크 생성
sudo ln -s /data/images /app/images

# 권한 설정
sudo chown -R ubuntu:ubuntu /data/images
```

**장점**: 
- 기존 설정 유지 가능
- 블록 스토리지 활용

## nginx 설정

nginx에서 `/images/` 경로를 블록 스토리지로 서빙하도록 설정:

```nginx
server {
    listen 443 ssl;
    server_name www.fitlink1207.store;

    # SSL 설정...

    # 이미지 파일 서빙
    location /images/ {
        alias /data/images/;
        expires 1y;
        add_header Cache-Control "public, immutable";
        
        # 파일이 없으면 404 반환 (Spring Boot로 전달하지 않음)
        try_files $uri =404;
    }

    # API 요청은 Spring Boot로 프록시
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 확인 사항

### 1. 디렉토리 존재 확인
```bash
ls -la /data/images
ls -la /app/images  # 방법 2 사용 시
```

### 2. 권한 확인
```bash
ls -ld /data/images
# 결과: drwxr-xr-x ... ubuntu ubuntu ... /data/images
```

### 3. 파일 저장 확인
```bash
# 파일이 실제로 저장되는지 확인
ls -la /data/images/
```

### 4. nginx 설정 테스트
```bash
# nginx 설정 문법 확인
sudo nginx -t

# nginx 재시작
sudo systemctl restart nginx
```

## 디버깅

### Spring Boot 로그 확인
```bash
# 애플리케이션 로그에서 파일 저장 경로 확인
tail -f /path/to/application.log | grep "file.dir"
```

### nginx 로그 확인
```bash
# nginx 에러 로그
sudo tail -f /var/log/nginx/error.log

# nginx 액세스 로그
sudo tail -f /var/log/nginx/access.log
```

### 파일 직접 접근 테스트
```bash
# 서버에서 직접 파일 확인
curl http://localhost:8080/images/파일명.png

# nginx를 통한 접근 테스트
curl https://www.fitlink1207.store/images/파일명.png
```

## 권장 설정 (최종)

### application.properties
```properties
file.dir=/data/images
file.url=/images/
file.base-url=https://www.fitlink1207.store
```

### nginx 설정
```nginx
location /images/ {
    alias /data/images/;
    expires 1y;
    add_header Cache-Control "public, immutable";
    try_files $uri =404;
}
```

이렇게 설정하면:
1. Spring Boot가 `/data/images`에 파일 저장
2. nginx가 `/data/images`에서 파일 서빙
3. URL은 `https://www.fitlink1207.store/images/파일명.png` 형태로 반환


