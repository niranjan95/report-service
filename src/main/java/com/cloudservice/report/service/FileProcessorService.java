package com.cloudservice.report.service;

import com.cloudservice.report.model.TradeData;
import lombok.extern.slf4j.Slf4j;
import org.beanio.BeanReader;
import org.beanio.StreamFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Component
@Slf4j
public class FileProcessorService implements ApplicationListener<ApplicationReadyEvent> {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50 MB in bytes

    @Value("${file.folder.path}")
    private String folderPath;

    @Value("${file.response.folder.path}")
    private String responsePath;

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

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            processExistingFiles();
            startFileWatcher();
        } catch (Exception e) {
            // TODO Auto-generated catch block
           log.error("Error occurred while reading file");
        }
    }

    private void processExistingFiles() throws IOException {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    processFile(file);
                }
            }
        }
    }

    private void startFileWatcher() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(folderPath);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            while (true) {
                WatchKey watchKey = watchService.take();
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path filePath = path.resolve((Path) event.context());
                        processFile(filePath.toFile());
                    }
                }
                watchKey.reset();
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error occurred while reading file");
        }
    }

    private void processFile(File file) throws IOException {
        // Add your file processing logic here
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
                responseGenerationService.generateResponseFile(trades, responsePath, file.getName());
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
