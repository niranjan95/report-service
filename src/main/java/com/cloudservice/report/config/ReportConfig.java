package com.cloudservice.report.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.cloudservice.report.model.InstrumentData;
import com.cloudservice.report.model.TradeData;
import org.beanio.StreamFactory;
import org.beanio.builder.CsvParserBuilder;
import org.beanio.builder.StreamBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportConfig {
    public static final AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

    @Bean
    public StreamFactory streamFactory() {
        StreamFactory streamFactory = StreamFactory.newInstance();
        streamFactory.define(new StreamBuilder("tradeData").format("delimited").
                parser(new CsvParserBuilder().delimiter(',').quote('"')).addRecord(TradeData.class));
        streamFactory.define(new StreamBuilder("instrumentData").format("delimited").
                parser(new CsvParserBuilder().delimiter(',').quote('"')).addRecord(InstrumentData.class));
        return streamFactory;
    }
   @Bean
    public AmazonS3 amazonS3Client() {
        return AmazonS3ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1").build();
    }
}
