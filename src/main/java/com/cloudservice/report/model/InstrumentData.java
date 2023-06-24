package com.cloudservice.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beanio.annotation.Field;
import org.beanio.annotation.Record;

@Data
@Builder
@Record(minOccurs = 1)
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentData {
    //Transaction ID1
    @Field(trim =true, at = 0)
    String assest;
    //Contract Type
    @Field(trim =true, at = 1)
    String isin;
    //Action type
    @Field(trim =true, at = 2)
    String classification;
    //UTI
    @Field(trim =true, at = 3)
    String loanBasedPrd;
    //Level
    @Field(trim =true, at = 4)
    String loanSubPrd;
    //Reporting Counterparty Code
    @Field(trim =true, at = 5)
    String loanFurSubPrd;
    //Reporting Counterparty Financial Status
    @Field(trim =true, at = 6)
    String loanLeiIssuer;
    //Reporting Counterparty Sector
    @Field(trim =true, at = 7)
    String loanMaturity;
    //Non-Reporting Counterparty Code
    @Field(trim =true, at = 8)
    String loanJurisdiction;
}
