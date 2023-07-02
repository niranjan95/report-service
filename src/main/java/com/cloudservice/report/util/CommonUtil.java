package com.cloudservice.report.util;

import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommonUtil {

    public static String getResponseFileName(MultipartFile file) {
        String name = file.getOriginalFilename();
        int lastDotIndex = name.lastIndexOf(".");
        if (lastDotIndex != -1) {
            String extension = name.substring(lastDotIndex);
            name = name.substring(0, lastDotIndex);
            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            String timeStamp = currentTime.format(formatter);
            return "RESP_" + name + "_" + timeStamp + extension;
        } else {
            throw new RuntimeException("File extension not found");
        }
    }
}
