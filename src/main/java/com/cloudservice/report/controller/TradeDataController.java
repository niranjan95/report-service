package com.cloudservice.report.controller;

import com.cloudservice.report.model.ProcessStatus;
import com.cloudservice.report.model.TradeData;
import com.cloudservice.report.model.TradeDataRequest;
import com.cloudservice.report.service.FileProcessorService;
import com.cloudservice.report.service.TradeDataService;
import com.cloudservice.report.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class TradeDataController {

    @Autowired
    private TradeDataService tradeDataService;

    @Autowired
    private FileProcessorService fileProcessorService;

    private static Map<String, ProcessStatus> PROCESS_STATUS_MAP = new HashMap<>();

    @PostMapping("/getTradeData")
    public List<TradeData> getTradData(@RequestBody TradeDataRequest tradeDataRequest) {
        return tradeDataService.fetchTradeData(tradeDataRequest);
    }

    @PostMapping(value = "/uploadTradeData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadTradeData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "file is empty";
        }
        String responseFileName = CommonUtil.getResponseFileName(file);
        PROCESS_STATUS_MAP.put(responseFileName, ProcessStatus.PROCESSING);
        CompletableFuture.runAsync(() -> {
            try {
                fileProcessorService.uploadAndProcessFile(file);
            } catch (Exception e) {
                throw new RuntimeException("Error occurred while processing file", e);
            }
        }).handle((result, exception) -> {
            if (exception == null) {
                PROCESS_STATUS_MAP.put(responseFileName, ProcessStatus.COMPLETED);
            } else {
                PROCESS_STATUS_MAP.put(responseFileName, ProcessStatus.FAILED);
                log.error("Error occurred", exception);
            }
            return null;
        });
        return String.format("Trade data file process started, Response file name is %s. " +
                "You can check its status using /fetchResponseStatus endpoint", responseFileName);
    }

    @PostMapping("/getResponseFileStatus")
    public String getTradData(@RequestParam("responseFileName") String responseFileName) {
        ProcessStatus processStatus = PROCESS_STATUS_MAP.get(responseFileName);
        if (processStatus != null) {
            return processStatus.name();
        } else {
            return "response file not found";
        }
    }
}
