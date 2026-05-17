package com.example.splitbill.user.repo;

import com.example.splitbill.user.domain.Settlements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementsRepository extends JpaRepository<Settlements, Long> {
}
