package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.client.FitnessVideoFeignClient;
import com.fitlink.web.dto.FitnessVideoResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {
    private final FitnessVideoFeignClient feignClient;

    @Value("${kf100.service-key}")
    private String serviceKey;

    /**
     * 컨트롤러 생성자.
     * @param feignClient 주입된 FeignClient 인스턴스
     */
    public VideoController(FitnessVideoFeignClient feignClient) {
        this.feignClient = feignClient;
    }

    /**
     * 국민체력100 동영상 목록을 HTTP GET 요청을 통해 조회함.
     *
     * @param pageNo 조회할 페이지 번호 (기본값 1)
     * @param numOfRows 한 페이지당 결과 수 (기본값 10)
     * @param fitnessFactor 검색 키워드 (선택 사항)
     * @return API 호출 결과를 담은 응답 엔티티 (HTTP 200 OK)
     */
    @GetMapping
    public ApiResponse<FitnessVideoResponseDTO> getVideos(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows,
            @RequestParam String fitnessFactor
    ) {

        FitnessVideoResponseDTO response = feignClient.getVideos(serviceKey, pageNo, numOfRows, fitnessFactor, "json");

        return ApiResponse.onSuccess(response);
    }
}
