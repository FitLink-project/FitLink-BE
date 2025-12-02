package com.fitlink.web.controller;

import com.fitlink.apiPayload.ApiResponse;
import com.fitlink.config.security.jwt.CustomUserDetails;
import com.fitlink.config.security.jwt.JwtTokenProvider;
import com.fitlink.domain.*;
import com.fitlink.domain.enums.Sex;
import com.fitlink.repository.FitnessResultRepository;
import com.fitlink.repository.TestGeneralRepository;
import com.fitlink.repository.TestKookminRepository;
import com.fitlink.repository.UsersInfoRepository;
import com.fitlink.service.fitness.FitnessScoreService;
import com.fitlink.service.fitness.standards.FitnessStandardSet;
import com.fitlink.service.fitness.standards.FitnessStandards;
import com.fitlink.web.dto.FitnessGeneralRequestDTO;
import com.fitlink.web.dto.FitnessKookminRequestDTO;
import com.fitlink.web.dto.FitnessResponseDTO;
import com.fitlink.web.mapper.FitnessMapper;
import com.fitlink.web.mapper.FitnessResultMapper;
import com.fitlink.web.mapper.UserInfoMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/fitness")
public class FitnessController {

    private final JwtTokenProvider jwtTokenProvider;
    private final FitnessResultMapper fitnessResultMapper;
    private final UserInfoMapper userInfoMapper;
    private final FitnessScoreService fitnessScoreService;
    private final FitnessResultRepository fitnessResultRepository;
    private final UsersInfoRepository usersInfoRepository;
    private final TestKookminRepository testKookminRepository;
    private final TestGeneralRepository testGeneralRepository;
    private final FitnessStandards fitnessStandards;
    private final FitnessMapper fitnessMapper;

    public FitnessController(
            JwtTokenProvider jwtTokenProvider,
            FitnessResultMapper fitnessResultMapper, UserInfoMapper userInfoMapper,
            FitnessScoreService fitnessScoreService,
            FitnessResultRepository fitnessResultRepository, UsersInfoRepository usersInfoRepository, TestKookminRepository testKookminRepository, TestGeneralRepository testGeneralRepository, FitnessStandards fitnessStandards, FitnessMapper fitnessMapper
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.fitnessResultMapper = fitnessResultMapper;
        this.userInfoMapper = userInfoMapper;
        this.fitnessScoreService = fitnessScoreService;
        this.fitnessResultRepository = fitnessResultRepository;
        this.usersInfoRepository = usersInfoRepository;
        this.testKookminRepository = testKookminRepository;
        this.testGeneralRepository = testGeneralRepository;
        this.fitnessStandards = fitnessStandards;
        this.fitnessMapper = fitnessMapper;
    }

    /**
     * users_info에 사용자의 성별, 키, 몸무게를 저장함.
     *
     * @param user   해당 사용자 엔티티
     * @param sexStr "M" 또는 "F"
     * @param height 키
     * @param weight 몸무게
     * @return UserInfo 객체
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
     * 국민체력100 측정 결과를 저장하거나 업데이트함.
     *
     * @param user    측정 결과 사용자 엔티티
     * @param request 악력, 윗몸일으키기 등 측정 데이터가 포함된 요청 DTO
     */
    private TestKookmin saveOrUpdateTestKookmin(Users user, FitnessKookminRequestDTO request) {

        // 유저의 가장 최신 기록 1개만 조회
        TestKookmin testKookmin = testKookminRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null);

        if (testKookmin == null) {
            // [CREATE] 기록이 없으므로 새로 빌드
            testKookmin = TestKookmin.builder()
                    .user(user)
                    // DTO가 Float라면 BigDecimal.valueOf()로 감싸야 함
                    .gripStrength(BigDecimal.valueOf(request.getGripStrength()))
                    .sitUp(request.getSitUp())
                    .sitAndReach(BigDecimal.valueOf(request.getSitAndReach()))
                    .shuttleRun(request.getShuttleRun())
                    .sprint(BigDecimal.valueOf(request.getSprint()))
                    .standingLongJump(BigDecimal.valueOf(request.getStandingLongJump()))
                    .build();
        } else {
            // [UPDATE] 기존 기록 값 변경
            testKookmin.setGripStrength(BigDecimal.valueOf(request.getGripStrength()));
            testKookmin.setSitUp(request.getSitUp());
            testKookmin.setSitAndReach(BigDecimal.valueOf(request.getSitAndReach()));
            testKookmin.setShuttleRun(request.getShuttleRun());
            testKookmin.setSprint(BigDecimal.valueOf(request.getSprint()));
            testKookmin.setStandingLongJump(BigDecimal.valueOf(request.getStandingLongJump()));
        }

        return testKookminRepository.save(testKookmin);
    }

    /**
     * 간단 체력 측정 결과를 저장하거나 업데이트함.
     *
     * @param user    측정 결과를 사용자 엔티티
     * @param request 슬라이더 점수, YMCA 스텝 테스트 등 측정 데이터가 포함된 요청 DTO
     */
    private TestGeneral saveOrUpdateTestGeneral(Users user, FitnessGeneralRequestDTO request) {

        // 유저의 가장 최신 기록 1개만 조회
        TestGeneral testGeneral = testGeneralRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElse(null); // 없으면 null 반환

        if (testGeneral == null) {
            // [CREATE] 기록이 없으므로 새로 빌드
            testGeneral = TestGeneral.builder()
                    .user(user)
                    .sliderStrength(request.getSliderStrength())     // Integer
                    .sitUp(request.getSitUp())                       // Integer
                    .sitAndReach(BigDecimal.valueOf(request.getSitAndReach())) // Float -> BigDecimal
                    .ymcaStepTest(BigDecimal.valueOf(request.getYmcaStepTest())) // Float -> BigDecimal
                    .sliderAgility(request.getSliderAgility())       // Integer
                    .sliderPower(request.getSliderPower())           // Integer (DTO 필드명이 sliderQuickness라면 수정 필요)
                    .build();
        } else {
            // [UPDATE] 기존 기록 값 변경
            testGeneral.setSliderStrength(request.getSliderStrength());
            testGeneral.setSitUp(request.getSitUp());
            testGeneral.setSitAndReach(BigDecimal.valueOf(request.getSitAndReach()));
            testGeneral.setYmcaStepTest(BigDecimal.valueOf(request.getYmcaStepTest()));
            testGeneral.setSliderAgility(request.getSliderAgility());
            testGeneral.setSliderPower(request.getSliderPower());
        }
        
        return testGeneralRepository.save(testGeneral);
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

        // 사용자의 운동 항목별 수치 저장
        TestKookmin saved = saveOrUpdateTestKookmin(user, request);

        // 점수 계산
        FitnessResponseDTO response = fitnessScoreService.calculateKookmin(request);

        // 엔티티 변환 후 저장
        FitnessResult entity = fitnessResultMapper.toEntity(response, user);
        entity.setKookminResultId(saved); // 국민체력100 측정 결과와 매핑
        fitnessResultRepository.save(entity);

        // users_info 테이블에 성별, 생년월일, 키, 몸무게 저장 후 응답 DTO에 추가
        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(user, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());
        response.setUserInfo(userInfo);

        response.setTestKookmin(fitnessMapper.toKookminDTO(saved));

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

        TestGeneral saved = saveOrUpdateTestGeneral(user, request);

        FitnessResponseDTO response = fitnessScoreService.calculateGeneral(request);

        FitnessResult entity = fitnessResultMapper.toEntity(response, user);
        entity.setGeneralResultId(saved); // 간단 체력 측정 결과와 매핑
        fitnessResultRepository.save(entity);

        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(user, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());
        response.setUserInfo(userInfo);

        response.setTestGeneral(fitnessMapper.toGeneralDTO(saved));

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

        // 측정 결과 수치 저장
        TestKookmin saved = saveOrUpdateTestKookmin(user, request);

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
        existing.setKookminResultId(saved);
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

        TestGeneral saved = saveOrUpdateTestGeneral(user,request);

        FitnessResult existing = existingList.stream()
                .max(Comparator.comparing(FitnessResult::getCreatedAt))
                .get();

        response = fitnessScoreService.calculateGeneral(request);

        FitnessResponseDTO.FitnessAverage average = response.getAverage();

        fitnessResultMapper.updateEntityFromResponse(response, existing);
        existing.setGeneralResultId(saved);
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

        // 평균값 추가
        int age = FitnessScoreService.calculateAge(Objects.requireNonNull(userInfo).getBirthDate());
        FitnessStandardSet st = fitnessStandards.getStandard(userInfo.getSex(), age);
        response.setAverage(fitnessScoreService.getAverage(st));

        if (entity.getKookminResultId() != null) {
            // 국민체력 100 결과 처리
            TestKookmin existing = testKookminRepository.findById(entity.getKookminResultId().getId())
                    .orElse(null);
            response.setTestKookmin(fitnessMapper.toKookminDTO(existing));

        } else {
            // 간단 체력 측정 결과 처리
            TestGeneral existing = testGeneralRepository.findById(entity.getGeneralResultId().getId())
                    .orElse(null);
            response.setTestGeneral(fitnessMapper.toGeneralDTO(existing));
        }

        return ApiResponse.onSuccess(response);
    }

}
