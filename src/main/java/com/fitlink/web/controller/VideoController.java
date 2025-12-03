package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.client.FitnessVideoFeignClient;
import com.fitlink.web.dto.FitnessVideoResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@RestController
@RequestMapping("/api/video")
public class VideoController {
    private final FitnessVideoFeignClient feignClient;

    @Value("${kf100.service-key}")
    private String serviceKey;

    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /**
     * 컨트롤러 생성자
     * @param feignClient 주입된 FeignClient 인스턴스
     */
    public VideoController(FitnessVideoFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    /**
     * 국민체력100 동영상 목록을 HTTP GET 요청을 통해 조회함
     *
     * @param pageNo 조회할 페이지 번호 기본값 1
     * @param numOfRows 한 페이지당 결과 수 기본값 10
     * @param fitnessFactor 검색 키워드 선택 사항
     * @return API 호출 결과를 담은 응답 엔티티 HTTP 200 OK
     */
    @GetMapping
    public ApiResponse<FitnessVideoResponseDTO> getVideos(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows,
            @RequestParam String fitnessFactor
    ) {
        // [Log] 요청 수신 로그
        log.info("[VideoController] 동영상 목록 조회 요청 - Page: {}, Rows: {}, Factor: {}", pageNo, numOfRows, fitnessFactor);

        FitnessVideoResponseDTO response = feignClient.getVideos(serviceKey, pageNo, numOfRows, fitnessFactor, "json");

        // [Log] 응답 성공 로그
        log.info("[VideoController] 동영상 목록 조회 성공 - TotalCount: {}", response.getResponse().getBody().getTotalCount());

        return ApiResponse.onSuccess(response);
    }

    /**
     * [Native Java 방식] 동영상 스트리밍 프록시
     * Feign을 거치지 않고 직접 연결하여 호환성 및 성능 문제를 해결합니다.
     */
    @GetMapping("/stream")
    public void streamVideo(@RequestParam("url") String videoUrl, HttpServletResponse response) {
        log.info("[VideoController] 스트리밍 요청: {}", videoUrl);

        HttpURLConnection connection = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(videoUrl);
            connection = (HttpURLConnection) url.openConnection();

            // 1. 헤더 설정 (브라우저인 척)
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);
            connection.setRequestProperty("Accept", "*/*");

            connection.connect();

            // 2. 응답 코드 확인
            int responseCode = connection.getResponseCode();

            // ★ [핵심 추가] 3xx 리다이렉트(301, 302) 발생 시 처리 로직
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == 307 ||
                    responseCode == 308) {

                String newUrl = connection.getHeaderField("Location");
                log.info("[VideoController] 리다이렉트 감지 ({} -> {}), 재연결 시도...", responseCode, newUrl);

                // 기존 연결 해제 후 새 URL로 연결
                url = new URL(newUrl);
                connection = (HttpURLConnection) url.openConnection();

                // 헤더 다시 설정 (필수)
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", USER_AGENT);
                connection.setRequestProperty("Accept", "*/*");

                connection.connect();
                responseCode = connection.getResponseCode(); // 새로운 응답 코드 확인
            }

            // 3. 최종 응답이 200이 아니면 에러 처리
            if (responseCode != 200) {
                log.error("원본 서버 응답 에러: {}", responseCode);
                response.sendError(responseCode, "원본 서버에서 영상을 가져올 수 없습니다.");
                return;
            }

            // 4. 헤더 설정 (브라우저 재생 유도)
            response.setContentType("video/mp4");
            response.setHeader("Content-Disposition", "inline");

            long contentLength = connection.getContentLengthLong();
            if (contentLength > 0) {
                response.setHeader("Content-Length", String.valueOf(contentLength));
            }

            // 5. 데이터 전송 (스트리밍)
            inputStream = connection.getInputStream();
            StreamUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();

            log.info("[VideoController] 스트리밍 전송 완료");

        } catch (Exception e) {
            // 클라이언트가 재생 중단(브라우저 닫기 등) 시 'Broken pipe' 에러가 날 수 있으나 자연스러운 현상입니다.
            log.warn("스트리밍 전송 중단 또는 오류: {}", e.getMessage());
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception ignored) {}
        }
    }
}