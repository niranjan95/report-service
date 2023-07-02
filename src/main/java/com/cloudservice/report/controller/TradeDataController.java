package com.cloudservice.report.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.cloudservice.report.model.ProcessStatus;
import com.cloudservice.report.model.TradeData;
import com.cloudservice.report.model.TradeDataRequest;
import com.cloudservice.report.service.FileProcessorService;
import com.cloudservice.report.service.TradeDataService;
import com.cloudservice.report.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
@CrossOrigin(origins = "*")
public class TradeDataController {

    @Autowired
    private TradeDataService tradeDataService;

    @Autowired
    private FileProcessorService fileProcessorService;

    @Autowired
    private AmazonS3 s3Client;

    @Value("${cloud.service.s3.validation_service.bucket}")
    private String s3Directory;

    @PostMapping("/getTradeData")
    public List<TradeData> getTradData(@RequestBody TradeDataRequest tradeDataRequest, HttpServletRequest request) {
        String clientId = request.getAttribute("clientId").toString();
        return tradeDataService.fetchTradeData(tradeDataRequest, clientId);
    }

    @PostMapping(value = "/uploadTradeData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadTradeData(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return "file is empty";
        }
        String clientId = request.getAttribute("clientId").toString();
        String responseFileName = CommonUtil.getResponseFileName(file);
        Path path = Paths.get(responseFileName);

        String responseFileNameWithoutExtention = FilenameUtils.removeExtension(path.getFileName().toString());
        uploadMetadataFile(clientId, responseFileNameWithoutExtention);


        CompletableFuture.runAsync(() -> {
            try {
                fileProcessorService.uploadAndProcessFile(file, clientId, responseFileName);
            } catch (Exception e) {
                throw new RuntimeException("Error occurred while processing file", e);
            }
        }).handle((result, exception) -> {
            if (exception == null) {
                log.error("{} processed success fully", responseFileName);
            } else {
                log.error("Error occurred for {}", responseFileName, exception);
            }
            s3Client.deleteObject(s3Directory, clientId + "/response/" + responseFileNameWithoutExtention + ".metadata");
            return null;
        });
        return String.format("Trade data file process started, response file name is %s,  " +
                "location %s", responseFileName, clientId + "/response/" + responseFileName);
    }

    private void uploadMetadataFile(String clientId, String responseFileNameWithoutExtention) {
        InputStream inputStream = new ByteArrayInputStream("metadata file".getBytes());

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");

        PutObjectRequest putObjectRequest = new PutObjectRequest(s3Directory, clientId + "/response/" + responseFileNameWithoutExtention + ".metadata", inputStream, metadata);
        s3Client.putObject(putObjectRequest);
    }

    @PostMapping("/getResponseFileStatus")
    public String getTradData(@RequestParam("responseFileName") String responseFileName, HttpServletRequest request) {
        String clientId = request.getAttribute("clientId").toString();
        if (StringUtils.isNotEmpty(responseFileName)) {
            Path path = Paths.get(responseFileName);
            String responseFileNameWithoutExtention = FilenameUtils.removeExtension(path.getFileName().toString());
            ObjectListing objectListing = s3Client.listObjects(s3Directory, clientId + "/response/" + responseFileNameWithoutExtention);
            if (!CollectionUtils.isEmpty(objectListing.getObjectSummaries())) {
                if (objectListing.getObjectSummaries().stream()
                        .anyMatch(s3ObjectSummary -> s3ObjectSummary.getKey().contains(responseFileName))) {
                    return ProcessStatus.SUCCESS.getMessage();
                } else if (objectListing.getObjectSummaries().stream()
                        .anyMatch(s3ObjectSummary -> s3ObjectSummary.getKey().contains((responseFileNameWithoutExtention + ".metadata")))) {
                    return ProcessStatus.PROCESSING.getMessage();
                } else {
                    return ProcessStatus.FAILED_OR_NOT_FOUND.getMessage();
                }
            } else {
                return ProcessStatus.FAILED_OR_NOT_FOUND.getMessage();
            }
        } else {
            return ProcessStatus.FAILED_OR_NOT_FOUND.getMessage();
        }
    }

    @GetMapping("/hello")
    public String hello(){
      return "hello";
    }
}
