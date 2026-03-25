package com.example.accountservice.repository;

import com.example.accountservice.entity.AccountAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountAuditRepository extends JpaRepository<AccountAudit, String> {
}