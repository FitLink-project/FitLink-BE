package com.fitlink.repository;

import com.fitlink.domain.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Integer> {
    Optional<Users> findByNickName(String nickName);
    Optional<Users> findByEmail(String email);
    Optional<Users> findById(Long id);

}

