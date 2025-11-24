package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.config.security.jwt.CustomUserDetails;
import com.fitlink.service.AIPrescriptionService;
import com.fitlink.web.dto.AIPrescriptionRequestDTO;
import com.fitlink.web.dto.AIPrescriptionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIPrescriptionController {

    private final AIPrescriptionService aiPrescriptionService;

    /**
     * AI 기반 운동 처방 생성
     * 사용자의 신체 정보(나이, 성별, 키, 몸무게)를 기반으로 맞춤형 운동 처방을 생성합니다.
     *
     * @param userDetails 인증된 사용자 정보
     * @param request 사용자 신체 정보
     * @return 운동 처방 (준비운동, 본운동, 마무리운동)
     */
    @PostMapping("/prescription")
    public ApiResponse<AIPrescriptionResponseDTO> getPrescription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid AIPrescriptionRequestDTO request) {
        
        AIPrescriptionResponseDTO response = aiPrescriptionService.getPrescription(request);
        return ApiResponse.onSuccess(response);
    }
}

