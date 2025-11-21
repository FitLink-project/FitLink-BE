package com.fitlink.client;

import com.fitlink.config.FeignConfig;
import com.fitlink.web.dto.FitnessVideoResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 서울올림픽기념국민체육진흥공단의 '국민체력100 동영상 정보' API 호출을 위한 Feign Client.
 */
@FeignClient(name = "kf100-video-api", url = "https://${kf100.base-url}", configuration = FeignConfig.class)
public interface FitnessVideoFeignClient {

    /**
     * 국민체력100 동영상 목록을 조회함.
     *
     * @param serviceKey 공공데이터포털에서 발급받은 서비스 키
     * @param pageNo 조회할 페이지 번호
     * @param numOfRows 한 페이지당 결과 수
     * @param fitnessFactor 검색할 동영상 제목 키워드
     * @param resultType 응답 데이터 형식 ("json" 또는 "xml")
     * @return 외부 API 응답을 매핑한 {@link FitnessVideoResponseDTO} 객체
     */
    @GetMapping(path = "/TODZ_VDO_FTNS_CERT_I", consumes = "text/json")
    FitnessVideoResponseDTO getVideos(
            @RequestParam("serviceKey") String serviceKey,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("numOfRows") int numOfRows,
            @RequestParam("ftns_fctr_nm") String fitnessFactor,
            @RequestParam("resultType") String resultType
    );
}