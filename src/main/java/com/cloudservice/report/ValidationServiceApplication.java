package com.cloudservice.report;

import com.amazonaws.services.s3.AmazonS3;
import com.cloudservice.report.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@SpringBootApplication
@Slf4j
@RestController
public class ValidationServiceApplication {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private AmazonS3 s3Client;

    public static void main(String[] args) {
        SpringApplication.run(ValidationServiceApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return "Welcome to validation service !!!";
    }

}
