package com.cloudservice.report.model;

public enum ProcessStatus {
    SUCCESS("Success"),
    PROCESSING("Processing"),
    FAILED_OR_NOT_FOUND("Failed Or Not Found");

    private String message;
    ProcessStatus(String message) {
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
