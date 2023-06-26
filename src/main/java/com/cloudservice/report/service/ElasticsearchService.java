package com.cloudservice.report.service;

import com.cloudservice.report.model.TradeData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ElasticsearchService {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${cloud.service.elasticsearch.trade.data.index}")
    String indexName;

    @Autowired
    private RestHighLevelClient client;

    public void uploadDocuments(List<TradeData> tradeDataList) {
        BulkRequest bulkRequest = new BulkRequest();
        tradeDataList.forEach(tradeData -> {
            IndexRequest indexRequest = new IndexRequest(indexName, "_doc", tradeData.getId());
            try {
                indexRequest.source(mapper.writeValueAsString(tradeData), XContentType.JSON);
                bulkRequest.add(indexRequest);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (bulkResponse.hasFailures()) {
            log.info("Bulk indexing failed: " + bulkResponse.buildFailureMessage());
        } else {
            log.info("Bulk indexing is successful");
        }
    }

    public List<TradeData> searchDocuments(QueryBuilder query) {
        List<TradeData> tradeDataList = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source().query(query);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            Arrays.stream(searchHits).forEach(searchHit -> {
                String source = searchHit.getSourceAsString();
                try {
                    tradeDataList.add(mapper.readValue(source, TradeData.class));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tradeDataList;
    }

}
