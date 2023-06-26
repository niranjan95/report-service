package com.cloudservice.report.service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.beanio.BeanReader;
import org.beanio.StreamFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.cloudservice.report.model.InstrumentData;
import com.cloudservice.report.model.TradeData;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TradeDataEnrichService implements ApplicationListener<ApplicationReadyEvent>  {

    @Value("${cloud.service.reference.data.dir}")
    private String referenceDataPath;
	
    @Autowired
    private StreamFactory streamFactory;
    
	private Map<String, InstrumentData> referenceDataMap = new HashMap<>();

	@Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
		
		AtomicInteger rowIndex = new AtomicInteger();
        try (Stream<String> stream = Files.lines(Paths.get(referenceDataPath))) {
            stream.forEach(row -> {
                if (rowIndex.get() == 0) {
                    rowIndex.set(1);
                } else {
                    BeanReader beanReader = streamFactory.createReader("instrumentData", new StringReader(row));
                    InstrumentData instrumentData = (InstrumentData) beanReader.read();
                    referenceDataMap.putIfAbsent(instrumentData.getIsin().trim(), instrumentData);
                }
            });
        } catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Error occurred while reading instrument data file");
		}
    }

    public void enrich(TradeData tradeData) {
    	String isin = tradeData.getSecurityIdentifier();
    	if (referenceDataMap.containsKey(isin)) {
            InstrumentData instrumentData = referenceDataMap.get(isin);
            if(isEmpty(tradeData.getClassificationOfSecurity())) {
            	tradeData.setClassificationOfSecurity(instrumentData.getClassification());
            }
            if(isEmpty(tradeData.getLoanBaseProduct())) {
            	tradeData.setLoanBaseProduct(instrumentData.getLoanBasedPrd());
            }
            if(isEmpty(tradeData.getLoanSubProduct())) {
            	tradeData.setLoanSubProduct(instrumentData.getLoanSubPrd());
            }
            if(isEmpty(tradeData.getLoanFurtherSubProduct())) {
            	tradeData.setLoanFurtherSubProduct(instrumentData.getLoanFurSubPrd());
            }
            if(isEmpty(tradeData.getLoanLeiOfIssuer())) {
            	tradeData.setLoanLeiOfIssuer(instrumentData.getLoanLeiIssuer());
            }
            if(isEmpty(tradeData.getLoanMaturityOfSecurity())) {
            	tradeData.setLoanMaturityOfSecurity(instrumentData.getLoanMaturity());
            }
            if(isEmpty(tradeData.getLoanJurisdictionOfIssuer())) {
            	tradeData.setLoanJurisdictionOfIssuer(instrumentData.getLoanJurisdiction());
            }
    	}
    }
}

