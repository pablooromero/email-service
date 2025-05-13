package com.email_service.email_service.models;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccreditationDataForPdf {
    private Long accreditationId;
    private String salePointName;
    private Long userId;
    private String userEmail;
    private Double amount;
    private LocalDateTime receiptDate;
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "AccreditationDataForPdf{" +
                "accreditationId=" + accreditationId +
                ", salePointName='" + salePointName + '\'' +
                ", userId=" + userId +
                ", userEmail='" + userEmail + '\'' +
                ", amount=" + amount +
                ", receiptDate=" + receiptDate +
                ", createdAt=" + createdAt +
                '}';
    }
}
