package com.zhuldyz.habittracker.repository;

import java.util.Optional;
import com.zhuldyz.habittracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
