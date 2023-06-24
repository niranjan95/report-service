package com.cloudservice.report.model;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;

@Data
public class TradeDataRequest {
    private String transactionID;
    private String reportingCounterPartyCode;
    private String nonReportingCounterPartyCode;
    private String securityIdentifier;
    private String eventDate;
}
