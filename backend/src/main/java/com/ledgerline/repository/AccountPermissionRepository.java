package com.ledgerline.repository;

import com.ledgerline.entity.Account;
import com.ledgerline.entity.AccountPermission;
import com.ledgerline.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountPermissionRepository extends JpaRepository<AccountPermission, Long> {

    List<AccountPermission> findByUserId(Long userId);

    Optional<AccountPermission> findByAccountIdAndUserId(Long accountId, Long userId);

    @Query("SELECT ap.account FROM AccountPermission ap WHERE ap.user.id = :userId AND ap.canView = true")
    List<Account> findPermittedAccountsByUserId(@Param("userId") Long userId);

    boolean existsByAccountIdAndUserId(Long accountId, Long userId);
}
