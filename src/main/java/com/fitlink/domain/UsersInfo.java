package com.fitlink.domain;

import com.fitlink.domain.enums.Sex;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users_info")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class UsersInfo {

    @Id
    @Column(name = "users_id")
    private Long usersId;

    // @MapsId: Users의 PK를 가져와서 자신의 PK이자 FK로 사용
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "users_id")
    private Users users;

    private Float height;

    private Float weight;

    @Column(length = 8) // YYYYMMDD
    private String birthDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('M', 'F')")
    private Sex sex;
}