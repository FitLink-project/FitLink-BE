# 커스텀 유효성 검사 구현체

이 폴더는 **커스텀 유효성 검사의 실제 로직**을 구현하는 곳입니다.

## 사용 목적

`annotation/` 폴더에서 정의한 커스텀 어노테이션의 **검증 로직을 구현**합니다.

## 구조

각 커스텀 어노테이션마다 하나의 Validator 클래스를 생성합니다.

- `UniqueEmail` 어노테이션 → `UniqueEmailValidator` 클래스
- `ValidPassword` 어노테이션 → `ValidPasswordValidator` 클래스

## 예시

### 1. 이메일 중복 검증 구현체

```java
// validator/UniqueEmailValidator.java
@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    
    private final UserRepository userRepository;
    
    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true; // null 체크는 @NotBlank에서 처리
        }
        
        return !userRepository.findByEmail(email).isPresent();
    }
}
```

### 2. 비밀번호 형식 검증 구현체

```java
// validator/ValidPasswordValidator.java
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {
    
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$");
    
    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true; // null 체크는 @NotBlank에서 처리
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
```

## 구현 규칙

1. `ConstraintValidator<어노테이션타입, 검증할필드타입>` 인터페이스 구현
2. `isValid()` 메서드에서 검증 로직 작성
3. `true` 반환 시 검증 성공, `false` 반환 시 검증 실패
4. 외부 의존성(Repository 등)이 필요한 경우 `@Component` 사용

## 에러 메시지 커스터마이징

```java
@Override
public boolean isValid(String email, ConstraintValidatorContext context) {
    boolean isValid = !userRepository.findByEmail(email).isPresent();
    
    if (!isValid) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("이미 사용 중인 이메일입니다.")
               .addConstraintViolation();
    }
    
    return isValid;
}
```
