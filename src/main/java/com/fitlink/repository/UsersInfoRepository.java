package com.fitlink.repository;

import com.fitlink.domain.UsersInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersInfoRepository extends JpaRepository<UsersInfo, Long> {
}
