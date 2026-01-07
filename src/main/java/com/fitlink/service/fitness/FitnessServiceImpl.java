package com.fitlink.service.fitness;

import com.fitlink.domain.*;
import com.fitlink.domain.enums.Sex;
import com.fitlink.repository.*;
import com.fitlink.service.fitness.standards.FitnessStandardSet;
import com.fitlink.service.fitness.standards.FitnessStandards;
import com.fitlink.web.dto.FitnessGeneralRequestDTO;
import com.fitlink.web.dto.FitnessKookminRequestDTO;
import com.fitlink.web.dto.FitnessResponseDTO;
import com.fitlink.web.mapper.FitnessMapper;
import com.fitlink.web.mapper.FitnessResultMapper;
import com.fitlink.web.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * 체력 측정(국민체력100, 일반 측정) 결과의 등록, 수정, 조회를 담당하는 서비스 구현체입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FitnessServiceImpl implements FitnessService {

    private final FitnessResultRepository fitnessResultRepository;
    private final UsersInfoRepository usersInfoRepository;
    private final TestKookminRepository testKookminRepository;
    private final TestGeneralRepository testGeneralRepository;
    private final UserRepository userRepository;

    private final FitnessScoreService fitnessScoreService;
    private final FitnessStandards fitnessStandards;

    private final FitnessMapper fitnessMapper;
    private final FitnessResultMapper fitnessResultMapper;
    private final UserInfoMapper userInfoMapper;

    /**
     * Detached 상태의 User 객체를 영속(Persistent) 상태로 전환하기 위해 DB에서 재조회합니다.
     * JPA 저장 시 'detached entity passed to persist' 에러 방지용입니다.
     */
    private Users findUser(Users user) {
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 국민체력 100 측정 결과를 등록합니다.
     */
    @Override
    public FitnessResponseDTO registerKookminTest(Users user, FitnessKookminRequestDTO request) {
        Users managedUser = findUser(user);

        // 1. 유저 기본 정보(키, 몸무게 등) 업데이트
        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(managedUser, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());

        // 2. 측정 데이터 저장
        TestKookmin savedTest = saveOrUpdateTestKookmin(managedUser, request);

        // 3. 점수 계산
        FitnessResponseDTO response = fitnessScoreService.calculateKookmin(request);

        // 4. 결과 엔티티 생성 및 저장
        FitnessResult entity = fitnessResultMapper.toEntity(response, managedUser);
        entity.setKookminResultId(savedTest);
        fitnessResultRepository.save(entity);

        // 5. 응답 조합
        response.setUserInfo(userInfo);
        response.setTestKookmin(fitnessMapper.toKookminDTO(savedTest));

        return response;
    }

    /**
     * 일반 체력 측정 결과를 등록합니다.
     */
    @Override
    public FitnessResponseDTO registerGeneralTest(Users user, FitnessGeneralRequestDTO request) {
        Users managedUser = findUser(user);

        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(managedUser, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());

        TestGeneral savedTest = saveOrUpdateTestGeneral(managedUser, request);
        FitnessResponseDTO response = fitnessScoreService.calculateGeneral(request);

        FitnessResult entity = fitnessResultMapper.toEntity(response, managedUser);
        entity.setGeneralResultId(savedTest);
        fitnessResultRepository.save(entity);

        response.setUserInfo(userInfo);
        response.setTestGeneral(fitnessMapper.toGeneralDTO(savedTest));

        return response;
    }

    /**
     * 기존 국민체력 100 측정 결과를 수정합니다.
     */
    @Override
    public FitnessResponseDTO updateKookminTest(Users user, FitnessKookminRequestDTO request) {
        Users managedUser = findUser(user);

        FitnessResult existingResult = findLatestFitnessResult(managedUser);

        TestKookmin savedTest = saveOrUpdateTestKookmin(managedUser, request);
        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(managedUser, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());

        FitnessResponseDTO response = fitnessScoreService.calculateKookmin(request);
        FitnessResponseDTO.FitnessStandardResponse standard = response.getStandard();

        fitnessResultMapper.updateEntityFromResponse(response, existingResult);
        existingResult.setKookminResultId(savedTest);
        fitnessResultRepository.save(existingResult);

        response = fitnessResultMapper.toResponseDTO(existingResult);
        response.setUserInfo(userInfo);
        response.setStandard(standard);

        return response;
    }

    /**
     * 기존 일반 체력 측정 결과를 수정합니다.
     */
    @Override
    public FitnessResponseDTO updateGeneralTest(Users user, FitnessGeneralRequestDTO request) {
        Users managedUser = findUser(user);

        FitnessResult existingResult = findLatestFitnessResult(managedUser);

        TestGeneral savedTest = saveOrUpdateTestGeneral(managedUser, request);
        FitnessResponseDTO.UserInfo userInfo = saveOrUpdateUsersInfo(managedUser, request.getSex(), request.getBirthDate(), request.getHeight(), request.getWeight());

        FitnessResponseDTO response = fitnessScoreService.calculateGeneral(request);
        FitnessResponseDTO.FitnessStandardResponse standard = response.getStandard();

        fitnessResultMapper.updateEntityFromResponse(response, existingResult);
        existingResult.setGeneralResultId(savedTest);
        fitnessResultRepository.save(existingResult);

        response = fitnessResultMapper.toResponseDTO(existingResult);
        response.setUserInfo(userInfo);
        response.setStandard(standard);

        return response;
    }

    /**
     * 사용자의 가장 최신 체력 측정 결과를 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public FitnessResponseDTO getLatestFitnessResult(Users user) {
        Users managedUser = findUser(user);

        FitnessResult entity = findLatestFitnessResult(managedUser);
        FitnessResponseDTO response = fitnessResultMapper.toResponseDTO(entity);

        UsersInfo userInfo = usersInfoRepository.findById(managedUser.getId()).orElse(null);
        if (userInfo != null) {
            response.setUserInfo(userInfoMapper.toDTO(userInfo));

            int age = FitnessScoreService.calculateAge(userInfo.getBirthDate());
            FitnessStandardSet st = fitnessStandards.getStandard(userInfo.getSex(), age);
            response.setStandard(fitnessScoreService.getStandards(st));
        }

        if (entity.getKookminResultId() != null) {
            TestKookmin testKookmin = testKookminRepository.findById(entity.getKookminResultId().getId()).orElse(null);
            response.setTestKookmin(fitnessMapper.toKookminDTO(testKookmin));
        } else if (entity.getGeneralResultId() != null) {
            TestGeneral testGeneral = testGeneralRepository.findById(entity.getGeneralResultId().getId()).orElse(null);
            response.setTestGeneral(fitnessMapper.toGeneralDTO(testGeneral));
        }

        return response;
    }

    // ================= Private Helper Methods =================

    /**
     * 사용자의 가장 최신 FitnessResult 엔티티를 조회합니다.
     */
    private FitnessResult findLatestFitnessResult(Users user) {
        List<FitnessResult> list = fitnessResultRepository.findByUser(user);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("저장된 측정 결과가 없습니다.");
        }
        return list.stream()
                .max(Comparator.comparing(FitnessResult::getCreatedAt))
                .orElseThrow(() -> new IllegalArgumentException("결과를 찾을 수 없습니다."));
    }

    /**
     * 사용자의 신체 정보(성별, 생년월일, 키, 몸무게)를 저장하거나 업데이트합니다.
     */
    private FitnessResponseDTO.UserInfo saveOrUpdateUsersInfo(Users user, String sexStr, String birthDate, Float height, Float weight) {
        UsersInfo userInfo = usersInfoRepository.findById(user.getId())
                .orElse(UsersInfo.builder().users(user).build());

        userInfo.setSex(Sex.valueOf(sexStr));
        userInfo.setBirthDate(birthDate);
        userInfo.setHeight(height);
        userInfo.setWeight(weight);

        return userInfoMapper.toDTO(usersInfoRepository.save(userInfo));
    }

    /**
     * 국민체력 100 측정 데이터를 저장하거나, 이미 최신 기록이 있다면 업데이트합니다.
     */
    private TestKookmin saveOrUpdateTestKookmin(Users user, FitnessKookminRequestDTO request) {
        TestKookmin testKookmin = testKookminRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElse(TestKookmin.builder().user(user).build());

        testKookmin.setGripStrength(BigDecimal.valueOf(request.getGripStrength()));
        testKookmin.setSitUp(request.getSitUp());
        testKookmin.setSitAndReach(BigDecimal.valueOf(request.getSitAndReach()));
        testKookmin.setShuttleRun(request.getShuttleRun());
        testKookmin.setSprint(BigDecimal.valueOf(request.getSprint()));
        testKookmin.setStandingLongJump(BigDecimal.valueOf(request.getStandingLongJump()));

        return testKookminRepository.save(testKookmin);
    }

    /**
     * 일반 체력 측정 데이터를 저장하거나, 이미 최신 기록이 있다면 업데이트합니다.
     */
    private TestGeneral saveOrUpdateTestGeneral(Users user, FitnessGeneralRequestDTO request) {
        TestGeneral testGeneral = testGeneralRepository.findTopByUserOrderByCreatedAtDesc(user)
                .orElse(TestGeneral.builder().user(user).build());

        testGeneral.setSliderStrength(request.getSliderStrength());
        testGeneral.setSitUp(request.getSitUp());
        testGeneral.setSitAndReach(BigDecimal.valueOf(request.getSitAndReach()));
        testGeneral.setYmcaStepTest(BigDecimal.valueOf(request.getYmcaStepTest()));
        testGeneral.setSliderAgility(request.getSliderAgility());
        testGeneral.setSliderPower(request.getSliderPower());

        return testGeneralRepository.save(testGeneral);
    }
}