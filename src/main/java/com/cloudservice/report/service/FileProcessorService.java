package com.cloudservice.report.service;

import com.cloudservice.report.model.TradeData;
import lombok.extern.slf4j.Slf4j;
import org.beanio.BeanReader;
import org.beanio.StreamFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Component
@Slf4j
public class FileProcessorService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB in bytes

    @Value("${cloud.service.source.data.dir}")
    private String sourceDataDir;

    @Value("${cloud.service.response.data.dir}")
    private String responseDataDir;

    @Autowired
    private StreamFactory streamFactory;

    @Autowired
    private TradeDataValidationService tradeDataValidationService;

    @Autowired
    private TradeDataEnrichService tradeDataEnrichService;

    @Autowired
    private ResponseGenerationService responseGenerationService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    public void uploadAndProcessFile(MultipartFile file) throws IOException {
        File uploadDir = new File(sourceDataDir);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        byte[] bytes = file.getBytes();
        Path path = Paths.get(sourceDataDir + "/" + file.getOriginalFilename());
        Files.write(path, bytes);
        processFile(path.toFile());
    }

    private void processFile(File file) throws IOException {
        log.info("Start of Processing file: " + file.getAbsolutePath());
        if (validateFileSize(file)) {
            throw new RuntimeException("File [{" + file.getAbsolutePath() + "}] size is beyond " + "50MB.");
        }
        AtomicInteger rowIndex = new AtomicInteger();
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
            List<TradeData> trades = new ArrayList<>();
            stream.forEach(row -> {
                if (rowIndex.get() == 0) {
                    rowIndex.set(1);
                } else {
                    BeanReader beanReader = streamFactory.createReader("tradeData", new StringReader(row));
                    TradeData tradeData = (TradeData) beanReader.read();
                    tradeDataEnrichService.enrich(tradeData);
                    tradeDataValidationService.validate(tradeData);
                    trades.add(tradeData);
                }
            });
            if (!trades.isEmpty()) {
                responseGenerationService.generateResponseFile(trades, responseDataDir, file.getName());
                elasticsearchService.uploadDocuments(trades);
            }
        }
        log.info("End of Processing file: " + file.getAbsolutePath());
    }

    private boolean validateFileSize(File file) {
        long fileSize = file.length();
        return fileSize > MAX_FILE_SIZE;
    }
}
