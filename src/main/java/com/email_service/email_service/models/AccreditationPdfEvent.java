package com.email_service.email_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccreditationPdfEvent {
    private String to;
    private String subject;
    private String bodyHeader;
    private AccreditationDataForPdf accreditationData;

    @Override
    public String toString() {
        return "AccreditationPdfEvent{" +
                "to='" + to + '\'' +
                ", subject='" + subject + '\'' +
                ", bodyHeader='" + bodyHeader + '\'' +
                ", accreditationData=" + accreditationData +
                '}';
    }
}
