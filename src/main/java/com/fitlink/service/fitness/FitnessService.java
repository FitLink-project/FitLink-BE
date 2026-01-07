package com.fitlink.service.fitness;

import com.fitlink.domain.Users;
import com.fitlink.web.dto.FitnessGeneralRequestDTO;
import com.fitlink.web.dto.FitnessKookminRequestDTO;
import com.fitlink.web.dto.FitnessResponseDTO;

/**
 * 체력 측정(국민체력100, 일반 측정) 결과의 등록, 수정, 조회를 담당하는 서비스 인터페이스
 */
public interface FitnessService {

    /**
     * 국민체력 100 측정 결과를 등록
     *
     * 사용자 정보(키, 몸무게 등)를 갱신하고, 측정 값을 저장한 뒤 점수를 계산
     *
     *
     * @param user    인증된 사용자 엔티티
     * @param request 국민체력 100 측정 데이터 (악력, 윗몸일으키기 등)
     * @return 계산된 점수와 저장된 결과 정보가 담긴 DTO
     */
    FitnessResponseDTO registerKookminTest(Users user, FitnessKookminRequestDTO request);

    /**
     * 일반 체력 측정(간편 측정) 결과를 등록
     *
     * 슬라이더 점수 및 YMCA 스텝 테스트 등의 결과를 저장하고 점수를 계산
     *
     *
     * @param user    인증된 사용자 엔티티
     * @param request 일반 체력 측정 데이터
     * @return 계산된 점수와 저장된 결과 정보가 담긴 DTO
     */
    FitnessResponseDTO registerGeneralTest(Users user, FitnessGeneralRequestDTO request);

    /**
     * 기존 국민체력 100 측정 결과를 수정
     *
     * 가장 최신의 측정 기록을 찾아 값을 덮어쓰고, 점수를 재계산
     *
     *
     * @param user    인증된 사용자 엔티티
     * @param request 수정할 국민체력 100 측정 데이터
     * @return 업데이트된 결과 및 재계산된 점수 DTO
     */
    FitnessResponseDTO updateKookminTest(Users user, FitnessKookminRequestDTO request);

    /**
     * 기존 일반 체력 측정 결과를 수정
     *
     * @param user    인증된 사용자 엔티티
     * @param request 수정할 일반 체력 측정 데이터
     * @return 업데이트된 결과 및 재계산된 점수 DTO
     */
    FitnessResponseDTO updateGeneralTest(Users user, FitnessGeneralRequestDTO request);

    /**
     * 사용자의 가장 최신 체력 측정 결과를 조회합니다.
     *
     * 국민체력 100 결과 혹은 일반 측정 결과 중 최신 항목을 반환하며,
     * 연령별 평균 데이터(Standard)도 함께 포함됩니다.
     *
     *
     * @param user 인증된 사용자 엔티티
     * @return 최신 체력 측정 결과 DTO
     */
    FitnessResponseDTO getLatestFitnessResult(Users user);
}