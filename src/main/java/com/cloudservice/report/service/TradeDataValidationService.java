package com.cloudservice.report.service;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.springframework.stereotype.Service;

import com.cloudservice.report.model.TradeData;

@Service
public class TradeDataValidationService {

	private static final int MAX_ISIN_LENGTH = 12;
	private static final int MAX_LEI_LENGTH = 20;
	private static final int OTHER_FIELD_LENGTH = 100;

	public TradeData validate(TradeData tradeData){
		validateAlphanumericData(tradeData);
		if(!"RJCT".equals(tradeData.getStatus())) {
			tradeData.setStatus("ACPT");
		}
		return tradeData;
	}

	public void validateAlphanumericData(TradeData tradeData) {
		if (isEmpty(tradeData.getSecurityIdentifier()) || !isValidAlphanumeric(tradeData.getSecurityIdentifier(), MAX_ISIN_LENGTH)
				|| !isValidAlphanumeric(tradeData.getLoanLeiOfIssuer(), MAX_LEI_LENGTH)
				|| isEmpty(tradeData.getTransactionID()) || !isValidAlphanumeric(tradeData.getTransactionID(), OTHER_FIELD_LENGTH)
				|| isEmpty(tradeData.getReportingCounterPartyCode()) || !isValidAlphanumeric(tradeData.getReportingCounterPartyCode(), MAX_LEI_LENGTH)
				|| isEmpty(tradeData.getNonReportingCounterPartyCode()) || !isValidAlphanumeric(tradeData.getNonReportingCounterPartyCode(), MAX_LEI_LENGTH)
				|| !isValidAlphanumeric(tradeData.getContractType(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getActionType(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getUti(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getLevel(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getReportingCounterPartyFinancialStatus(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getCounterPartySector(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getNonReportingCounterPartyFinancialStatus(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getNonReportingCounterPartySector(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getCounterPartySide(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getTradingVenue(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getMasterAgreementType(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getGeneralCollateralInd(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getTypeOfAsset(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getClassificationOfSecurity(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getLoanBaseProduct(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getLoanSubProduct(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getLoanFurtherSubProduct(), OTHER_FIELD_LENGTH)
				|| !isValidAlphanumeric(tradeData.getLoanJurisdictionOfIssuer(), OTHER_FIELD_LENGTH)
				|| !isValidDate(tradeData.getEventDate())
				|| !isValidDate(tradeData.getValueDate())
				|| !isValidDate(tradeData.getLoanMaturityOfSecurity())) {
			tradeData.setStatus("RJCT");
		}
		if(!"RJCT".equals(tradeData.getStatus())) {
			tradeData.setStatus("ACPT");
		}
	}

	private boolean isValidAlphanumeric(String value, int maxLength) {
		// Validate alphanumeric with length check
		return value.matches("[a-zA-Z0-9]+") && value.length() <= maxLength;
	}

	private boolean isValidDate(String value) {
		// Validate date format yyyy-mm-dd
		return value.matches("\\d{4}-\\d{2}-\\d{2}");
	}
}
