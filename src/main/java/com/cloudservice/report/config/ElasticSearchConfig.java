package com.cloudservice.report.config;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@Slf4j
public class ElasticSearchConfig {
    @Value("${cloud.service.elasticsearch.host}")
    String endpoint;

    @Value("${cloud.service.elasticsearch.trade.data.index}")
    String indexName;

    private void initIndex(RestHighLevelClient client) {
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

            if (!client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {
                String mappingJson = IOUtils.toString(
                        new ClassPathResource("trade_data_mappings.json").getInputStream(),
                        StandardCharsets.UTF_8
                );
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
                createIndexRequest.mapping(mappingJson, XContentType.JSON);
                CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
                boolean acknowledged = createIndexResponse.isAcknowledged();
                if (acknowledged) {
                    log.info("Index creation acknowledged.");
                } else {
                    log.info("Index creation failed.");
                }
            }
            GetIndexRequest request = new GetIndexRequest("*"); // "*" retrieves all indices
            GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);

            // Get the list of indices from the response
            String[] indices = response.getIndices();

            // Print the list of indices
            log.info("Indices: " + Arrays.toString(indices));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        AWS4Signer signer = new AWS4Signer();
        signer.setServiceName("es");
        signer.setRegionName("eu-north-1");
        HttpRequestInterceptor httpRequestInterceptor = new AWSRequestSigningInterceptor("es", signer, new DefaultAWSCredentialsProviderChain());
        RestClientBuilder restClientBuilder = RestClient.builder(HttpHost.create(endpoint));
        restClientBuilder.setHttpClientConfigCallback(callback -> callback.addInterceptorLast(httpRequestInterceptor));
        RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);
        initIndex(client);
        return new RestHighLevelClient(restClientBuilder);
    }
}