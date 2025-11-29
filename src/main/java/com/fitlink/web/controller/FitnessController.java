package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.config.security.jwt.CustomUserDetails;
import com.fitlink.config.security.jwt.JwtTokenProvider;
import com.fitlink.domain.FitnessResult;
import com.fitlink.domain.Users;
import com.fitlink.domain.UsersInfo;
import com.fitlink.domain.enums.Sex;
import com.fitlink.repository.FitnessResultRepository;
import com.fitlink.repository.UsersInfoRepository;
import com.fitlink.service.fitness.FitnessScoreService;
import com.fitlink.web.dto.FitnessGeneralRequestDTO;
import com.fitlink.web.dto.FitnessKookminRequestDTO;
import com.fitlink.web.dto.FitnessResponseDTO;
import com.fitlink.web.mapper.FitnessResultMapper;
import com.fitlink.web.mapper.UserInfoMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/fitness")
public class FitnessController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FitnessResultMapper fitnessResultMapper;
    private final UserInfoMapper userInfoMapper;
    private final FitnessScoreService fitnessScoreService;
    private final FitnessResultRepository fitnessResultRepository;
    private final UsersInfoRepository usersInfoRepository;

    public FitnessController(
            JwtTokenProvider jwtTokenProvider,
            FitnessResultMapper fitnessResultMapper, UserInfoMapper userInfoMapper,
            FitnessScoreService fitnessScoreService,
            FitnessResultRepository fitnessResultRepository, UsersInfoRepository usersInfoRepository
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.fitnessResultMapper = fitnessResultMapper;
        this.userInfoMapper = userInfoMapper;
        this.fitnessScoreService = fitnessScoreService;
        this.fitnessResultRepository = fitnessResultRepository;
        this.usersInfoRepository = usersInfoRepository;
    }

    /**
     * users_info에 사용자의 성별, 키, 몸무게를 저장함.
     *
     * @param user   해당 사용자 엔티티
     * @param sexStr "M" 또는 "F"
     * @param height 키
     * @param weight 몸무게
     * @return
     */
    private FitnessResponseDTO.UserInfo saveOrUpdateUsersInfo(Users user, String sexStr, String birthDate, Float height, Float weight) {
        UsersInfo userInfo = usersInfoRepository.findById(user.getId())
                .orElse(UsersInfo.builder()
                        .users(user)
                        .build());

        // 값 업데이트
        userInfo.setSex(Sex.valueOf(sexStr)); // Enum 변환
        userInfo.setBirthDate(birthDate);
        userInfo.setHeight(height);
        userInfo.setWeight(weight);

        userInfo = usersInfoRepository.save(userInfo);
        return userInfoMapper.toDTO(userInfo);
    }

    /**
     * JWT 토큰을 이용해 인증된 사용자 정보를 추출하고 반환함.
     *
     * @param request HTTP 요청 객체
     * @return 인증된 사용자 Users 엔티티
     */
    private Users getUserFromRequest(HttpServletRequest request) {
        Authentication authentication = jwtTokenProvider.extractAuthentication(request);
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return principal.getUsers();
    }

    /**
     * 국민체력 100 종목 측정값을 기반으로 점수를 계산하고 DB에 새로운 결과를 저장함.
     *
     * @param request DTO 형태의 국민체력 100 측정값
     * @param httpServletRequest JWT가 포함된 HTTP 요청 객체
     * @return 계산된 점수와 결과 정보
     */
    @PostMapping("/kookmin")
    public ApiResponse<FitnessResponseDTO> postFitnessKf100(
            @RequestBody FitnessKookminRequestDTO request,
            HttpServletRequest httpServletRequest) {

        Users user = getUserFromRequest(httpServletRequest);

        // 점수 계산
        FitnessResponseDTO response = fitnessScoreService.calculateKookmin(request);

        // 엔티티 변환 후 저장
        FitnessResult entity = fitnessResultMapper.toEntity(response, user);
        fitnessResultRepository.save(entity);

        // users_info 테이블에 성별, 생년월일, 키, 몸무게 저장 후 응답 DTO에 추가
        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(user, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());
        response.setUserInfo(userInfo);

        return ApiResponse.onSuccess(response);
    }

    /**
     * 일반 체력 평가(General) 측정값을 기반으로 점수를 계산하고 DB에 새로운 결과를 저장함.
     *
     * @param request 일반 체력 측정값 DTO
     * @return 계산된 결과 DTO
     */
    @PostMapping("/general")
    public ApiResponse<FitnessResponseDTO> postFitnessGeneral(
            @RequestBody FitnessGeneralRequestDTO request,
            HttpServletRequest httpServletRequest) {

        Users user = getUserFromRequest(httpServletRequest);

        FitnessResponseDTO response = fitnessScoreService.calculateGeneral(request);

        FitnessResult entity = fitnessResultMapper.toEntity(response, user);
        fitnessResultRepository.save(entity);

        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(user, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());
        response.setUserInfo(userInfo);

        return ApiResponse.onSuccess(response);
    }

    /**
     * 국민체력 100 측정값을 기존 결과에 반영해 업데이트함.
     *
     * @param request 새로 전달된 국민체력 100 측정값 DTO
     * @return 업데이트된 전체 결과 DTO
     */
    @PatchMapping("/kookmin")
    public ApiResponse<FitnessResponseDTO> patchFitnessKf100(
            @RequestBody FitnessKookminRequestDTO request,
            HttpServletRequest httpServletRequest) {

        Users user = getUserFromRequest(httpServletRequest);
        FitnessResponseDTO response = null;

        // 기존 결과 조회 (중복 레코드 대비 안전하게 처리)
        List<FitnessResult> existingList = fitnessResultRepository.findByUser(user);
        if (existingList.isEmpty()) {
            return ApiResponse.onFailure("NOT_FOUND", "저장된 측정 결과가 없습니다.", response);
        }

        // 최신 레코드 선택
        FitnessResult existing = existingList.stream()
                .max(Comparator.comparing(FitnessResult::getCreatedAt))
                .get();

        // 새로운 점수 계산
        response = fitnessScoreService.calculateKookmin(request);

        // 점수를 계산하고 나온 평균값
        FitnessResponseDTO.FitnessAverage average = response.getAverage();

        // 기존 엔티티에 업데이트 적용
        fitnessResultMapper.updateEntityFromResponse(response, existing);
        fitnessResultRepository.save(existing);

        // 업데이트 된 전체 결과 DTO
        response = fitnessResultMapper.toResponseDTO(existing);

        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(user, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());
        response.setUserInfo(userInfo);

        // 결과 DTO에 평균값 추가
        response.setAverage(average);

        return ApiResponse.onSuccess(response);
    }

    /**
     * 일반 체력 측정값을 기존 결과에 업데이트함.
     *
     * @param request DTO 형태의 일반 체력 측정값
     * @return 업데이트된 전체 결과 DTO
     */
    @PatchMapping("/general")
    public ApiResponse<FitnessResponseDTO> patchFitnessGeneral(
            @RequestBody FitnessGeneralRequestDTO request,
            HttpServletRequest httpServletRequest) {

        Users user = getUserFromRequest(httpServletRequest);
        FitnessResponseDTO response = null;

        List<FitnessResult> existingList = fitnessResultRepository.findByUser(user);
        if (existingList.isEmpty()) {
            return ApiResponse.onFailure("NOT_FOUND", "저장된 측정 결과가 없습니다.", response);
        }

        FitnessResult existing = existingList.stream()
                .max(Comparator.comparing(FitnessResult::getCreatedAt))
                .get();

        response = fitnessScoreService.calculateGeneral(request);

        FitnessResponseDTO.FitnessAverage average = response.getAverage();

        fitnessResultMapper.updateEntityFromResponse(response, existing);
        fitnessResultRepository.save(existing);

        response = fitnessResultMapper.toResponseDTO(existing);

        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(user, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());
        response.setUserInfo(userInfo);

        response.setAverage(average);

        return ApiResponse.onSuccess(response);
    }

    /**
     * 인증된 사용자의 최신 체력 측정 결과를 조회해 반환함.
     *
     * @return 저장된 체력 평가 결과 DTO
     */
    @GetMapping("/result")
    public ApiResponse<FitnessResponseDTO> getFitnessResult(HttpServletRequest httpServletRequest) {

        Users user = getUserFromRequest(httpServletRequest);
        FitnessResponseDTO response = null;

        List<FitnessResult> entityList = fitnessResultRepository.findByUser(user);
        if (entityList.isEmpty()) {
            return ApiResponse.onFailure("NOT_FOUND", "조회 가능한 결과가 없습니다.", response);
        }

        // 최신 레코드 선택
        FitnessResult entity = entityList.stream()
                .max(Comparator.comparing(FitnessResult::getCreatedAt))
                .get();

        response = fitnessResultMapper.toResponseDTO(entity);

        // 사용자 정보 가져오고 응답 DTO에 추가
        UsersInfo userInfo = usersInfoRepository.findById(user.getId()).orElse(null);
        response.setUserInfo(userInfoMapper.toDTO(userInfo));

        return ApiResponse.onSuccess(response);
    }

}
