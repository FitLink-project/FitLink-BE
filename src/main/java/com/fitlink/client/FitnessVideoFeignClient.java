package com.fitlink.client;

import com.fitlink.config.FeignConfig;
import com.fitlink.web.dto.FitnessVideoResponseDTO;
import feign.Headers;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

/**
 * 서울올림픽기념국민체육진흥공단의 국민체력100 동영상 정보 API 호출을 담당하는 Feign Client 인터페이스임
 */
@FeignClient(name = "kf100-video-api", url = "https://${kf100.base-url}", configuration = FeignConfig.class)
public interface FitnessVideoFeignClient {

    /**
     * 국민체력100 동영상 목록을 조회함
     *
     * @param serviceKey 공공데이터포털에서 발급받은 서비스 키
     * @param pageNo 조회할 페이지 번호
     * @param numOfRows 한 페이지당 결과 수
     * @param fitnessFactor 검색할 동영상 제목 키워드
     * @param resultType 응답 데이터 형식 json 또는 xml
     * @return 외부 API 응답을 매핑한 FitnessVideoResponseDTO 객체
     */
    @GetMapping(path = "/TODZ_VDO_FTNS_CERT_I", consumes = "text/json")
    FitnessVideoResponseDTO getVideos(
            @RequestParam("serviceKey") String serviceKey,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("numOfRows") int numOfRows,
            @RequestParam("ftns_fctr_nm") String fitnessFactor,
            @RequestParam("resultType") String resultType
    );

    /**
     * 동영상 다운로드 (User-Agent 위장 필수!)
     */
    @GetMapping
    @Headers({
            // 크롬 브라우저인 척 User-Agent 설정
            "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            // 멀티미디어 요청임을 명시
            "Accept: */*"
    })
    Response downloadVideo(URI uri);
}