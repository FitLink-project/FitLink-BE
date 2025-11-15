# 커스텀 유효성 검사 어노테이션

이 폴더는 **커스텀 유효성 검사 어노테이션**을 작성하는 곳입니다.

## 사용 목적

기본 Jakarta Validation (`@NotBlank`, `@Email`, `@NotNull` 등)으로는 처리하기 어려운 
**비즈니스 로직 검증**이나 **재사용 가능한 검증 규칙**을 구현할 때 사용합니다.

## 구조

- `annotation/`: 커스텀 검증 어노테이션 정의
- `validator/`: 검증 로직 구현

## 예시

### 1. 이메일 중복 체크 (`@UniqueEmail`)

```java
// annotation/UniqueEmail.java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "이미 사용 중인 이메일입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

### 2. 비밀번호 형식 검증 (`@ValidPassword`)

```java
// annotation/ValidPassword.java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPasswordValidator.class)
public @interface ValidPassword {
    String message() default "비밀번호는 8자 이상, 영문과 숫자를 포함해야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## 사용 방법

DTO에 어노테이션을 추가하고, `@Valid`와 함께 사용:

```java
public class UserRequestDTO {
    @Getter
    @Setter
    public static class JoinDTO {
        @NotBlank
        String name;
        
        @NotBlank
        @Email
        @UniqueEmail  // 커스텀 검증
        String email;
        
        @NotBlank
        @ValidPassword  // 커스텀 검증
        String password;
    }
}
```

## 장점

1. **코드 재사용성**: 여러 DTO에서 동일한 검증 규칙 사용 가능
2. **선언적 검증**: DTO에 어노테이션만 추가하면 자동 검증
3. **컨트롤러 단순화**: 서비스 계층의 중복 검증 로직 제거
4. **표준화**: 일관된 에러 처리
