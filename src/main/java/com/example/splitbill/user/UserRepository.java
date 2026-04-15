package com.example.splitbill.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByMobileNumber(String mobileNumber);

    Optional<User> findUserByEmailId(String emailId);

    Optional<User> findUserById(long userId);
}
