매핑테이블 작성하는 곳입니다.

## MapStruct 사용 방법

이 프로젝트는 Entity와 DTO 간의 매핑을 위해 MapStruct를 사용합니다.

### 사용 예시

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper; // MapStruct가 자동 생성한 구현체 주입
    
    public UserResponseDTO.JoinResultDTO join(UserRequestDTO.JoinDTO joinDTO) {
        // DTO -> Entity 변환
        Users user = userMapper.toEntity(joinDTO);
        user.setRegDate(LocalDateTime.now());
        user.setRole(Role.USER);
        user.setIsActive(true);
        
        Users savedUser = userRepository.save(user);
        
        // Entity -> DTO 변환
        return userMapper.toJoinResultDTO(savedUser);
    }
    
    public UserResponseDTO.LoginResultDTO login(UserRequestDTO.LoginRequestDTO loginDTO) {
        Users user = userRepository.findByEmail(loginDTO.getEmail())
            .orElseThrow(() -> new GeneralException(ErrorCode.USER_NOT_FOUND));
        
        UserResponseDTO.LoginResultDTO result = userMapper.toLoginResultDTO(user);
        result.setAccessToken(jwtTokenProvider.generateToken(user));
        
        return result;
    }
}
```

### 주요 매핑 메서드

- `toEntity(UserRequestDTO.JoinDTO)`: JoinDTO를 Users 엔티티로 변환
- `toJoinResultDTO(Users)`: Users 엔티티를 JoinResultDTO로 변환
- `toLoginResultDTO(Users)`: Users 엔티티를 LoginResultDTO로 변환

### 참고사항

- MapStruct는 컴파일 타임에 매핑 구현체를 자동 생성합니다.
- `@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)`로 Spring Bean으로 등록됩니다.
- 필드명이 다를 경우 `@Mapping` 어노테이션으로 매핑 규칙을 지정할 수 있습니다.