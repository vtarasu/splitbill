package com.example.splitbill.user.repo;

import com.example.splitbill.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByMobileNumber(String mobileNumber);

    Optional<User> findUserByEmailId(String emailId);

    Optional<User> findUserById(long userId);
}
