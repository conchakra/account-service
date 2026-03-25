package com.example.accountservice.repository;

import com.example.accountservice.entity.AccountSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountSeriesRepository extends JpaRepository<AccountSeries, String> {
    Optional<AccountSeries> findByIsActiveTrue();
}