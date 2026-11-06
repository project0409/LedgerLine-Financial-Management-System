package com.ledgerline.service;

import com.ledgerline.entity.AuditLog;
import com.ledgerline.entity.User;
import com.ledgerline.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(User user, String action, String resourceType, String resourceId, String ipAddress, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .ipAddress(ipAddress)
                    .description(description)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit: user={} action={} resource={}:{} ip={}",
                    user != null ? user.getEmail() : "anonymous", action, resourceType, resourceId, ipAddress);
        } catch (Exception e) {
            log.error("Failed to persist audit log entry: action={}, resource={}:{}", action, resourceType, resourceId, e);
        }
    }
}
