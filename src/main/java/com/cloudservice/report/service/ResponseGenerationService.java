package com.cloudservice.report.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cloudservice.report.model.TradeData;
import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResponseGenerationService {
	public void generateResponseFile(List<TradeData> trades, String filePath, String name) { 
		File parentDir = new File(filePath);
		if (!parentDir.exists()) {
			boolean created = parentDir.mkdirs();
			if (!created) {
				log.error("Failed to create parent directories.");
				return;
			}
		}
		int lastDotIndex = name.lastIndexOf(".");
		if(lastDotIndex != -1) {
			String extension = name.substring(lastDotIndex, name.length());
			name = name.substring(0, lastDotIndex);
			LocalDateTime currentTime = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String timeStamp = currentTime.format(formatter);
			try (CSVWriter writer = new CSVWriter(new FileWriter(filePath +  "RESP_" + name + "_" + timeStamp + extension))) {
				// Write headers
				String[] headers = { "Record Number", "Transaction Id", "Status" };
				long row_num = 1;
				writer.writeNext(headers);
				// Write data records
				for (TradeData trade : trades) {
					String[] rowData = { "" + row_num++, trade.getTransactionID(), trade.getStatus() };
					writer.writeNext(rowData);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}