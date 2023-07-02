package com.cloudservice.report.service;

import com.amazonaws.services.s3.AmazonS3;
import com.cloudservice.report.model.TradeData;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ResponseGenerationService {

    @Value("${cloud.service.s3.validation_service.bucket}")
    private String s3Directory;

    @Autowired
    private AmazonS3 s3Client;

    public void generateResponseFile(List<TradeData> trades, String filePath, String clientId, String responseFileName) {
        String responseDataDir = filePath + "/" + clientId + "/response";
        File parentDir = new File(responseDataDir);
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                log.error("Failed to create parent directories.");
                return;
            }
        }
        String fileNameWithPath = responseDataDir + "/" + responseFileName;
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileNameWithPath))) {
            // Write headers
            String[] headers = {"Record Number", "Transaction Id", "Status"};
            long row_num = 1;
            writer.writeNext(headers);
            // Write data records
            for (TradeData trade : trades) {
                String[] rowData = {"" + row_num++, trade.getTransactionID(), trade.getStatus()};
                writer.writeNext(rowData);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        s3Client.putObject(s3Directory, clientId + "/response/" + responseFileName, new File(fileNameWithPath));
        try {
            Files.delete(Paths.get(fileNameWithPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}