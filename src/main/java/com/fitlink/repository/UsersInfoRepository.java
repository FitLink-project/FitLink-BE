package com.fitlink.repository;

import com.fitlink.domain.Users;
import com.fitlink.domain.UsersInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersInfoRepository extends JpaRepository<UsersInfo, Long> {
    Optional<UsersInfo> findByUsers(Users users);
}
