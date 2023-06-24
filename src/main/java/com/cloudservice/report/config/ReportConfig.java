package com.cloudservice.report.config;

import com.cloudservice.report.model.InstrumentData;
import com.cloudservice.report.model.TradeData;
import org.beanio.StreamFactory;
import org.beanio.builder.CsvParserBuilder;
import org.beanio.builder.StreamBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportConfig {

    @Bean
    public StreamFactory streamFactory() {
        StreamFactory streamFactory = StreamFactory.newInstance();
        streamFactory.define(new StreamBuilder("tradeData").format("delimited").
                parser(new CsvParserBuilder().delimiter(',').quote('"')).addRecord(TradeData.class));
        streamFactory.define(new StreamBuilder("instrumentData").format("delimited").
                parser(new CsvParserBuilder().delimiter(',').quote('"')).addRecord(InstrumentData.class));
        return streamFactory;
    }
}
