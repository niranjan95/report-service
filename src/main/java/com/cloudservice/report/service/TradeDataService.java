package com.cloudservice.report.service;

import com.cloudservice.report.model.TradeData;
import com.cloudservice.report.model.TradeDataRequest;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeDataService {

    @Autowired
    private ElasticsearchService elasticsearchService;

    public List<TradeData> fetchTradeData(TradeDataRequest tradeDataRequest) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if(StringUtils.isNotEmpty(tradeDataRequest.getTransactionID())) {
            boolQuery.must(QueryBuilders.termQuery("transactionID", tradeDataRequest.getTransactionID()));
        }
        if(StringUtils.isNotEmpty(tradeDataRequest.getReportingCounterPartyCode())) {
            boolQuery.must(QueryBuilders.termQuery("reportingCounterPartyCode", tradeDataRequest.getReportingCounterPartyCode()));
        }
        if(StringUtils.isNotEmpty(tradeDataRequest.getNonReportingCounterPartyCode())) {
            boolQuery.must(QueryBuilders.termQuery("nonReportingCounterPartyCode", tradeDataRequest.getNonReportingCounterPartyCode()));
        }
        if(StringUtils.isNotEmpty(tradeDataRequest.getSecurityIdentifier())) {
            boolQuery.must(QueryBuilders.termQuery("securityIdentifier", tradeDataRequest.getSecurityIdentifier()));
        }
        if(StringUtils.isNotEmpty(tradeDataRequest.getEventDate())) {
            boolQuery.must(QueryBuilders.termQuery("eventDate", tradeDataRequest.getEventDate()));
        }
        return elasticsearchService.searchDocuments(boolQuery);
    }
}
