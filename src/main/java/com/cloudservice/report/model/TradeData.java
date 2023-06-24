package com.cloudservice.report.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

import java.util.UUID;

@Data
@Builder
@Record(minOccurs = 1)
@AllArgsConstructor
@NoArgsConstructor
@Hidden
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeData {
    //Transaction ID
    @Field(trim = true, at = 0)
    String transactionID;
    //Contract Type
    @Field(trim = true, at = 1)
    String contractType;
    //Action type
    @Field(trim = true, at = 2)
    String actionType;
    //UTI
    @Field(trim = true, at = 3)
    String uti;
    //Level
    @Field(trim = true, at = 4)
    String level;
    //Reporting Counterparty Code
    @Field(trim = true, at = 5)
    String reportingCounterPartyCode;
    //Reporting Counterparty Financial Status
    @Field(trim = true, at = 6)
    String reportingCounterPartyFinancialStatus;
    //Reporting Counterparty Sector
    @Field(trim = true, at = 7)
    String counterPartySector;
    //Non-Reporting Counterparty Code
    @Field(trim = true, at = 8)
    String nonReportingCounterPartyCode;
    // Non-Reporting Counterparty Financial Status
    @Field(trim = true, at = 9)
    String nonReportingCounterPartyFinancialStatus;
    // Non-Reporting Counterparty Sector
    @Field(trim = true, at = 10)
    String nonReportingCounterPartySector;
    // Counterparty Side
    @Field(trim = true, at = 11)
    String counterPartySide;
    // Event date
    @Field(trim = true, at = 12)
    String eventDate;
    // Trading venue
    @Field(trim = true, at = 13)
    String tradingVenue;
    //Master agreement type
    @Field(trim = true, at = 14)
    String masterAgreementType;
    // Value date
    @Field(trim = true, at = 15)
    String valueDate;
    // General collateral Indicator
    @Field(trim = true, at = 16)
    String generalCollateralInd;
    // Type of asset
    @Field(trim = true, at = 17)
    String typeOfAsset;
    // Security identifier
    @Field(trim = true, at = 18)
    String securityIdentifier;
    // Classification of a security
    @Field(trim = true, at = 19)
    String classificationOfSecurity;
    // Loan Base product
    @Field(trim = true, at = 20)
    String loanBaseProduct;
    // Loan Sub product
    @Field(trim = true, at = 21)
    String loanSubProduct;
    // Loan Further sub product
    @Field(trim = true, at = 22)
    String loanFurtherSubProduct;
    //Loan LEI of the issuer
    @Field(trim = true, at = 23)
    String loanLeiOfIssuer;
    // Loan Maturity of the security
    @Field(trim = true, at = 24)
    String loanMaturityOfSecurity;
    // Loan Jurisdiction of the issuer
    @Field(trim = true, at = 25)
    String loanJurisdictionOfIssuer;

    public String getId() {
        return String.format("%s-%s-%s-%s-%s", transactionID, reportingCounterPartyCode, nonReportingCounterPartyCode, securityIdentifier, eventDate);
    }

    private String status;
}