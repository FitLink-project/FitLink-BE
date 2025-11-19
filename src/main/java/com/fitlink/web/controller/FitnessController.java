package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.service.fitness.FitnessCalculator;
import com.fitlink.service.fitness.FitnessScoreService;
import com.fitlink.service.fitness.standards.FitnessStandards;
import com.fitlink.web.dto.FitnessGeneralRequestDTO;
import com.fitlink.web.dto.FitnessKookminRequestDTO;
import com.fitlink.web.dto.FitnessResponseDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fitness")
public class FitnessController {
    private static final FitnessScoreService fitnessScoreService = new FitnessScoreService(new FitnessCalculator(), new FitnessStandards());

    @PostMapping("/kookmin")
    public ApiResponse<FitnessResponseDTO> postFitnessKf100(@RequestBody FitnessKookminRequestDTO request) {
        FitnessResponseDTO response;
        response = fitnessScoreService.calculateKookmin(request);
        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/general")
    public ApiResponse<FitnessResponseDTO> postFitnessGeneral(@RequestBody FitnessGeneralRequestDTO request) {
        FitnessResponseDTO response;
        response = fitnessScoreService.calculateGeneral(request);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/kookmin")
    public ApiResponse<FitnessResponseDTO> patchFitnessKf100(@RequestBody FitnessKookminRequestDTO request) {
        FitnessResponseDTO response = new FitnessResponseDTO();
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/general")
    public ApiResponse<FitnessResponseDTO> patchFitnessGeneral(@RequestBody FitnessGeneralRequestDTO request) {
        FitnessResponseDTO response = new FitnessResponseDTO();
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/result")
    public ApiResponse<FitnessResponseDTO> getFitnessResult() {
        FitnessResponseDTO response = new FitnessResponseDTO();
        return ApiResponse.onSuccess(response);
    }
}
