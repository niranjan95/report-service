package com.cloudservice.report.controller;

import com.cloudservice.report.model.TradeData;
import com.cloudservice.report.model.TradeDataRequest;
import com.cloudservice.report.service.TradeDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TradeDataController {

    @Autowired
    private TradeDataService tradeDataService;

    @PostMapping("/getTradeData")
    public List<TradeData> getTradData(@RequestBody TradeDataRequest tradeDataRequest) {
        return tradeDataService.fetchTradeData(tradeDataRequest);
    }
}
