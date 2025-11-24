package com.fitlink.service;

import com.fitlink.util.ExerciseMappingUtil;
import com.fitlink.web.dto.AIPrescriptionRequestDTO;
import com.fitlink.web.dto.AIPrescriptionResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Weka 모델을 사용하여 운동 처방을 예측하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIPrescriptionServiceImpl implements AIPrescriptionService {

    private final ExerciseMappingUtil exerciseMappingUtil;

    private Classifier prepModel;
    private Classifier mainModel;
    private Classifier coolDownModel;
    private Instances prepHeader;
    private Instances mainHeader;
    private Instances coolDownHeader;

    @PostConstruct
    public void init() {
        try {
            // 모델 파일 로드
            prepModel = loadModel("ai/models/Prep-LMT.model");
            mainModel = loadModel("ai/models/Main-LMT.model");
            coolDownModel = loadModel("ai/models/CoolDown-LMT.model");

            // 각 모델별 헤더 정보 생성
            // 학습 시 사용한 ARFF 파일의 헤더를 별도 파일로 저장하여 사용하는 것이 가장 안전합니다.
            // 헤더 파일이 없으면 모델에서 헤더 정보를 추출하거나 기본 구조를 생성합니다.
            prepHeader = createHeaderFromModel(prepModel, "Prep");
            mainHeader = createHeaderFromModel(mainModel, "Main");
            coolDownHeader = createHeaderFromModel(coolDownModel, "CoolDown");

            log.info("AI Prescription models loaded successfully");
            log.info("Header info - Prep: {} attributes, Main: {} attributes, CoolDown: {} attributes",
                    prepHeader.numAttributes(), mainHeader.numAttributes(), coolDownHeader.numAttributes());
        } catch (Exception e) {
            log.error("Failed to load AI Prescription models", e);
            throw new RuntimeException("Failed to initialize AIPrescriptionService", e);
        }
    }

    /**
     * 모델 파일 로드
     */
    private Classifier loadModel(String resourcePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        InputStream inputStream = resource.getInputStream();
        Object obj = SerializationHelper.read(inputStream);
        inputStream.close();
        return (Classifier) obj;
    }

    /**
     * 모델에서 헤더 정보를 추출하거나 생성
     * 
     * 주의: 실제 Weka 모델을 사용하려면 학습 시 사용한 ARFF 파일의 헤더 정보가 필요합니다.
     * 가장 좋은 방법은 학습 시 사용한 ARFF 파일의 헤더를 별도 파일로 저장하여 사용하는 것입니다.
     */
    private Instances createHeaderFromModel(Classifier model, String type) throws Exception {
        // 방법 1: 모델에서 헤더 정보 추출 시도
        try {
            // LMT 모델은 내부적으로 training data의 헤더를 포함할 수 있습니다.
            // 리플렉션을 사용하여 헤더 정보에 접근 시도
            java.lang.reflect.Method getHeaderMethod = model.getClass().getMethod("getHeader");
            if (getHeaderMethod != null) {
                Instances header = (Instances) getHeaderMethod.invoke(model);
                if (header != null) {
                    log.info("Extracted header from {} model: {} attributes", type, header.numAttributes());
                    return new Instances(header, 0);
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract header from model {}: {}", type, e.getMessage());
        }
        
        // 방법 2: 학습 시 사용한 ARFF 헤더 파일이 있다면 로드
        // 헤더 파일 경로: ai/models/header_{type}.arff
        try {
            ClassPathResource headerResource = new ClassPathResource("ai/models/header_" + type + ".arff");
            if (headerResource.exists()) {
                DataSource source = new DataSource(headerResource.getInputStream());
                Instances data = source.getDataSet();
                
                // 각 모델별로 사용하는 클래스 속성 인덱스 설정
                // Prep: Prep_Exercise (인덱스 4)
                // Main: Main_Exercise (인덱스 5)
                // CoolDown: CoolDown_Exercise (인덱스 6)
                int classIndex = switch (type) {
                    case "Prep" -> 4;      // Prep_Exercise
                    case "Main" -> 5;      // Main_Exercise
                    case "CoolDown" -> 6;  // CoolDown_Exercise
                    default -> data.numAttributes() - 1;
                };
                
                data.setClassIndex(classIndex);
                Instances header = new Instances(data, 0);
                log.info("Loaded header from file for {} model: {} attributes, class index: {}", 
                        type, header.numAttributes(), classIndex);
                return header;
            }
        } catch (Exception e) {
            log.debug("Header file not found for {} model: {}", type, e.getMessage());
        }
        
        // 방법 3: 기본 헤더 구조 생성 (임시 방편)
        // 실제 학습 데이터 구조에 맞게 수정 필요
        // 에러 메시지에서 7개 속성을 기대한다고 했으므로, 7개 속성으로 생성
        log.warn("Using default header structure for {} model. This may not match the training data structure.", type);
        
        StringBuilder arff = new StringBuilder();
        arff.append("@relation exercise_prescription\n\n");
        arff.append("@attribute age numeric\n");
        arff.append("@attribute gender numeric\n");
        arff.append("@attribute height numeric\n");
        arff.append("@attribute weight numeric\n");
        
        // 모델이 7개 속성을 기대한다면, 추가 속성이 필요합니다.
        // 실제 학습 데이터 구조를 확인하여 정확한 속성을 추가해야 합니다.
        // 여기서는 임시로 missing value를 처리할 수 있는 속성을 추가합니다.
        arff.append("@attribute measure_001 numeric\n");
        arff.append("@attribute measure_002 numeric\n");
        
        // 클래스 속성 - 실제 클래스 목록을 사용해야 합니다.
        // 매핑 파일에서 클래스 목록을 가져와서 사용하는 것이 좋습니다.
        arff.append("@attribute exercise {Placeholder}\n\n");
        arff.append("@data\n");
        arff.append("0,0,0,0,0,0,Placeholder\n");

        // 임시 파일로 저장하여 DataSource로 읽기
        java.io.File tempFile = java.io.File.createTempFile("header_" + type, ".arff");
        try (java.io.FileWriter writer = new java.io.FileWriter(tempFile, StandardCharsets.UTF_8)) {
            writer.write(arff.toString());
        }

        DataSource source = new DataSource(tempFile.getAbsolutePath());
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);

        // 헤더 생성 (데이터 없이 구조만)
        Instances header = new Instances(data, 0);
        
        // 임시 파일 삭제
        if (tempFile.exists()) {
            tempFile.delete();
        }

        return header;
    }

    /**
     * 예측 수행
     */
    @Override
    public AIPrescriptionResponseDTO getPrescription(AIPrescriptionRequestDTO request) {
        try {
            // 입력값 검증
            validateRequest(request);

            // 준비운동 예측 - Top 3 (기타 제외)
            List<String> prepExercises = predictExerciseTopN(prepModel, prepHeader, request, "Prep", 3);
            
            // 본운동 예측 - Top 3 (기타 제외)
            List<String> mainExercises = predictExerciseTopN(mainModel, mainHeader, request, "Main", 3);
            
            // 마무리운동 예측 - Top 3 (기타 제외)
            List<String> coolDownExercises = predictExerciseTopN(coolDownModel, coolDownHeader, request, "CoolDown", 3);

            // 영어 결과를 한국어로 변환하고 리스트로 분리 (개수 제한)
            List<String> warmupList = convertToKoreanListFromMultiple(prepExercises, "Prep", 3);
            List<String> mainExerciseList = convertToKoreanListFromMultiple(mainExercises, "Main", 2);
            List<String> cooldownList = convertToKoreanListFromMultiple(coolDownExercises, "CoolDown", 3);

            return AIPrescriptionResponseDTO.builder()
                    .warmup(warmupList)
                    .mainExercise(mainExerciseList)
                    .cooldown(cooldownList)
                    .build();

        } catch (Exception e) {
            log.error("Error during prescription prediction", e);
            throw new RuntimeException("Failed to generate prescription", e);
        }
    }

    /**
     * 단일 모델로 예측 수행 - Top-N 결과 반환 (기타 제외)
     */
    private List<String> predictExerciseTopN(Classifier model, Instances header, AIPrescriptionRequestDTO request, String type, int topN) throws Exception {
        // Instance 생성
        Instance instance = new DenseInstance(header.numAttributes());
        instance.setDataset(header);

        // 속성 값 설정 (모델 학습 시 사용한 순서와 동일해야 함)
        // ARFF 파일 구조: Age, Gender, Measure_001, Measure_002, Prep_Exercise, Main_Exercise, CoolDown_Exercise
        // DTO 구조: age, gender, height, weight
        // 매핑: Age = age, Gender = gender, Measure_001 = height, Measure_002 = weight
        instance.setValue(0, request.getAge().doubleValue());        // Age
        instance.setValue(1, request.getGender().doubleValue());   // Gender
        instance.setValue(2, request.getHeight().doubleValue());     // Measure_001 (height)
        instance.setValue(3, request.getWeight().doubleValue());     // Measure_002 (weight)
        
        // 클래스 속성들은 예측 대상이므로 missing value로 설정
        instance.setMissing(4);  // Prep_Exercise
        instance.setMissing(5);  // Main_Exercise
        instance.setMissing(6);  // CoolDown_Exercise

        // 확률 분포 가져오기
        double[] distribution = model.distributionForInstance(instance);
        
        // 확률과 인덱스를 함께 저장할 리스트 생성
        List<PredictionScore> scores = new ArrayList<>();
        for (int i = 0; i < distribution.length; i++) {
            String className = header.classAttribute().value(i);
            // "기타"와 "?" 제외
            if (!"Other".equals(className) && !"?".equals(className)) {
                scores.add(new PredictionScore(i, distribution[i], className));
            }
        }
        
        // 확률 내림차순 정렬
        scores.sort((a, b) -> Double.compare(b.probability, a.probability));
        
        // Top-N 추출
        List<String> topResults = new ArrayList<>();
        int count = Math.min(topN, scores.size());
        for (int i = 0; i < count; i++) {
            topResults.add(scores.get(i).className);
        }
        
        log.debug("Predicted {} exercise Top-{}: {}", type, topN, topResults);
        
        return topResults;
    }
    
    /**
     * 예측 점수를 저장하는 내부 클래스
     */
    private static class PredictionScore {
        int index;
        double probability;
        String className;
        
        PredictionScore(int index, double probability, String className) {
            this.index = index;
            this.probability = probability;
            this.className = className;
        }
    }

    /**
     * 여러 영어 운동 이름을 한국어로 변환하고 리스트로 분리
     * 각 운동 이름은 쉼표로 구분된 여러 운동을 포함할 수 있음
     * 
     * @param englishExercisesList 영어 운동 이름 리스트
     * @param type 운동 타입 (Prep, Main, CoolDown)
     * @param maxCount 최대 반환 개수
     * @return 한국어로 변환된 운동 리스트 (최대 maxCount개)
     */
    private List<String> convertToKoreanListFromMultiple(List<String> englishExercisesList, String type, int maxCount) {
        if (englishExercisesList == null || englishExercisesList.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();

        for (String englishExercises : englishExercisesList) {
            if (englishExercises == null || englishExercises.trim().isEmpty() || "?".equals(englishExercises)) {
                continue;
            }

            // 작은따옴표 제거 및 공백 정리
            String cleaned = englishExercises.replace("'", "").trim();
            
            // 먼저 전체 문자열을 매핑 시도 (쉼표로 구분된 여러 운동이 하나의 클래스인 경우)
            String koreanFull = switch (type) {
                case "Prep" -> exerciseMappingUtil.toKoreanPrep(cleaned);
                case "Main" -> exerciseMappingUtil.toKoreanMain(cleaned);
                case "CoolDown" -> exerciseMappingUtil.toKoreanCoolDown(cleaned);
                default -> cleaned;
            };
            
            // 전체 문자열이 매핑되었고, 원본과 다르면 (매핑 성공)
            if (!koreanFull.equals(cleaned)) {
                // 매핑된 결과를 쉼표로 분리하여 추가
                String[] koreanExercises = koreanFull.split(",");
                for (String korean : koreanExercises) {
                    String trimmed = korean.trim();
                    if (!trimmed.isEmpty() && !"기타".equals(trimmed) && !"Other".equals(trimmed) && !"?".equals(trimmed)) {
                        if (!result.contains(trimmed) && result.size() < maxCount) {
                            result.add(trimmed);
                        }
                    }
                    // 개수 제한에 도달하면 중단
                    if (result.size() >= maxCount) {
                        break;
                    }
                }
            } else {
                // 전체 매핑 실패 시, 쉼표로 구분하여 각각 매핑 시도
                String[] exercises = cleaned.split(",");
                
                for (String exercise : exercises) {
                    String trimmed = exercise.trim();
                    if (!trimmed.isEmpty() && !"Other".equals(trimmed) && !"?".equals(trimmed)) {
                        String korean = switch (type) {
                            case "Prep" -> exerciseMappingUtil.toKoreanPrep(trimmed);
                            case "Main" -> exerciseMappingUtil.toKoreanMain(trimmed);
                            case "CoolDown" -> exerciseMappingUtil.toKoreanCoolDown(trimmed);
                            default -> trimmed;
                        };
                        // 매핑이 성공했으면 (원본과 다름) 추가
                        if (!korean.equals(trimmed)) {
                            // 중복 제거 및 개수 제한
                            if (!result.contains(korean) && result.size() < maxCount) {
                                result.add(korean);
                            }
                        }
                    }
                    // 개수 제한에 도달하면 중단
                    if (result.size() >= maxCount) {
                        break;
                    }
                }
            }
            
            // 개수 제한에 도달하면 중단
            if (result.size() >= maxCount) {
                break;
            }
        }

        // 최대 개수만큼만 반환
        return result.size() > maxCount ? result.subList(0, maxCount) : result;
    }

    /**
     * 요청 데이터 검증
     */
    private void validateRequest(AIPrescriptionRequestDTO request) {
        if (request.getAge() == null || request.getAge() <= 0) {
            throw new IllegalArgumentException("Age must be a positive number");
        }
        if (request.getGender() == null || (request.getGender() != 0 && request.getGender() != 1)) {
            throw new IllegalArgumentException("Gender must be 0 (female) or 1 (male)");
        }
        if (request.getHeight() == null || request.getHeight() <= 0) {
            throw new IllegalArgumentException("Height must be a positive number");
        }
        if (request.getWeight() == null || request.getWeight() <= 0) {
            throw new IllegalArgumentException("Weight must be a positive number");
        }
    }
}

