package com.ledgerline.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    private String accountName;

    @NotBlank(message = "Account type is required")
    @Pattern(regexp = "^(CHECKING|SAVINGS|BUSINESS|INVESTMENT)$", message = "Account type must be CHECKING, SAVINGS, BUSINESS, or INVESTMENT")
    private String accountType;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code (e.g., USD, EUR)")
    @Builder.Default
    private String currency = "USD";
}
