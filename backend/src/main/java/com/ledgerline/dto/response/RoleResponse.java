package com.ledgerline.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {

    private Long id;
    private String roleName;
    private String description;
    private LocalDateTime createdAt;
}
