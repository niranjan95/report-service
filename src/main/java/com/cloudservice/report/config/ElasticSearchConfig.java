package com.cloudservice.report.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Configuration
@Slf4j
public class ElasticSearchConfig {
    @Value("${cloud.service.elasticsearch.host}")
    String endpoint;
    @Value("${cloud.service.elasticsearch.username}")
    String username;
    @Value("${cloud.service.elasticsearch.password}")
    String password;

    @Value("${cloud.service.elasticsearch.trade.data.index}")
    String indexName;

    private void initIndex(RestHighLevelClient client){
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);

            if (!client.indices().exists(getIndexRequest, RequestOptions.DEFAULT)) {String mappingJson = IOUtils.toString(
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
        try {
            SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build();
            RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(endpoint, 443, "https"))
                    .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(getCredentialsProvider(username, password)).setSSLContext(sslContext));
            RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);
            initIndex(client);
            return new RestHighLevelClient(restClientBuilder);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    private CredentialsProvider getCredentialsProvider(String username, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        return credentialsProvider;
    }
}