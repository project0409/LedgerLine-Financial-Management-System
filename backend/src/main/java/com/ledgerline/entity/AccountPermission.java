package com.ledgerline.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "account_permissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_account_user_permission", columnNames = {"account_id", "user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AccountPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "can_view", nullable = false)
    @Builder.Default
    private Boolean canView = true;

    @Column(name = "can_deposit", nullable = false)
    @Builder.Default
    private Boolean canDeposit = false;

    @Column(name = "can_withdraw", nullable = false)
    @Builder.Default
    private Boolean canWithdraw = false;

    @Column(name = "can_transfer", nullable = false)
    @Builder.Default
    private Boolean canTransfer = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by")
    private User grantedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
