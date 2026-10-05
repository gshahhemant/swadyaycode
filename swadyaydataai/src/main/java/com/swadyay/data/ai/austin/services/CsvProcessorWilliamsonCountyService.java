package com.swadyay.data.ai.austin.services;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.swadyay.data.ai.austin.entity.ContactAustinEntity;
import com.swadyay.data.ai.austin.entity.ContactId;
import com.swadyay.data.ai.austin.repository.ContactAustinRepository;
import com.swadyay.data.ai.austin.util.OllamaAIService;
import com.swadyay.data.ai.common.entity.FileProcessLog;
import com.swadyay.data.ai.common.service.FileProcessLogService;
import com.swadyay.data.ai.sanantonio.controller.Constant;
import com.swadyay.data.ai.sanantonio.services.PropertyRecord;

@Service
public class CsvProcessorWilliamsonCountyService {
	
	 private static final Logger logger = LoggerFactory.getLogger(CsvProcessorWilliamsonCountyService.class);

	@Autowired
	OllamaAIService openAIService;

	@Autowired
	ContactAustinRepository contactRepository;

	@Autowired
	FileProcessLogService fileProcessLogService;

	private final String[] headers = { "Property ID", "Geographic ID", "Type", "Property Address", "Legal Description",
			"Owner Name", "Doing Business As", "Appraised Value" };

	public void processCsv(String inputFile,String zipCode) {

		LocalDateTime start = LocalDateTime.now();

		logger.info("process started TIME::: " + LocalDateTime.now());

		FileProcessLog fileProcessLog = fileProcessLogService.startLog(inputFile);

		String zip = zipCode;
		String outputFile = "src/main/resources/" + inputFile + "_processed.csv";

		inputFile = "src/main/resources/" + inputFile + ".csv";

		List<PropertyRecord> validRecords = new ArrayList<>();

		int i = 1;
		int j = 1;
		int k = 1;
		try (BufferedReader rawReader = Files.newBufferedReader(Paths.get(inputFile))) {
			List<String[]> cleanLines = new ArrayList<>();
			String line;
			int lineNumber = 0;
			int expectedFieldCount = 8;

			while ((line = rawReader.readLine()) != null) {
				lineNumber++;

				
				if (line.trim().isEmpty())
					continue;

				try (CSVReader csvReader = new CSVReader(new StringReader(line))) {
					String[] fields = csvReader.readNext();

					if (fields == null)
						continue;

					cleanLines.add(fields);
				} catch (Exception e) {
					System.err.println("⚠️ Skipped bad line " + lineNumber + ": " + e.getMessage());

					System.err.println(line);

				}
			}

			logger.info("total clened size:::" + cleanLines.size());
			boolean result = false;
			// ✅ Convert to POJO & filter
			
		
			String ownerAddress = null;
			for (String[] fields : cleanLines) {

				PropertyRecord record = new PropertyRecord();
				record.setPropertyId(fields[0]);
				record.setOwnerName(fields[2]);
				record.setPropertyAddress(fields[4]);
				record.setLegalDescription(fields[5]);
				ownerAddress = fields[3];

				try {
					boolean sameAddress = doesAddressMatch(record.getPropertyAddress(), ownerAddress);

					logger.info("************OwnerName*****" + record.getOwnerName() + " propertyID::"
							+ record.getPropertyId());

					logger.info("************Owner Address*****" + ownerAddress);
					logger.info("************Suit Address *****" + record.getPropertyAddress());

					if (sameAddress) {

						result = openAIService.isIndianName(record.getOwnerName());

						if (result) {

							logger.info("************Indian ownerName*****[" + j + "]::" + record.getOwnerName());
							validRecords.add(record);
							j++;

						}

					} else {
						logger.info("$$$$$$$$$$$$$ Rental Home $$$$$$$$$$$$" + ownerAddress);
						logger.info("$$$$$$$$ ownerName $$$$$$$$  " + ownerAddress);
						logger.info("$$$$$$$$ Suit Address $$$$$$$$ " + record.getPropertyAddress());

						logger.info(
								"Total rental Homes  record[" + k + "] of total records [" + cleanLines.size() + "]");
					}

					logger.info("processed record[" + i + "] of total records [" + cleanLines.size() + "]");

				} catch (Exception e) {

					logger.info("Failed processing CSV" + e);
					e.printStackTrace();

				}
				i++;

			}

			logger.info("total Indian names for  :::" + validRecords.size());

			// ✅ Sort
			validRecords = validRecords.stream().sorted(
					Comparator.comparing(PropertyRecord::getGeographicId, Comparator.nullsLast(String::compareTo)))
					.collect(Collectors.toList());

			// ✅ Write output
			try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
				writer.writeNext(headers);
				for (PropertyRecord record : validRecords) {
					writer.writeNext(new String[] { record.getPropertyId(), record.getGeographicId(), record.getType(),
							record.getPropertyAddress(), record.getLegalDescription(), record.getOwnerName(),
							record.getDoingBusinessAs() });
				}
			}

			logger.info("✅ Output written to: " + outputFile);

			dumpDataToDatabase(validRecords, zip);

			fileProcessLogService.completeLog(fileProcessLog, cleanLines.size());

		} catch (Exception e) {

			logger.info("Failed processing CSV" + e);
			e.printStackTrace();
			fileProcessLogService.failLog(fileProcessLog);
		}

		logger.info("process complated  TIME::: " + LocalDateTime.now());

		LocalDateTime end = LocalDateTime.now();

		// Calculate duration
		Duration duration = Duration.between(start, end);
		long totalSeconds = duration.getSeconds();

		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;

		// Print nicely
		logger.info("Total Records::: "+i);
		logger.info(String.format("⏱️ Process took: %02d hour(s), %02d minute(s), %02d second(s)", hours, minutes, seconds));

	}

	public void dumpDataToDatabase(List<PropertyRecord> validRecords, String zip) {
		int i = 0;
		int j=0;
		for (PropertyRecord validRecord : validRecords) {

			ContactAustinEntity contactAustinEntity = contactRepository
					.findById(new ContactId(validRecord.getPropertyId(), zip))
					.orElse(null);

			if (contactAustinEntity == null) {
				
				// Create new Contact 
				contactAustinEntity = new ContactAustinEntity();
				
				contactAustinEntity.setProperty_id(validRecord.getPropertyId());
				
				contactAustinEntity.setName(validRecord.getOwnerName());
				contactAustinEntity.setProperty_address(validRecord.getPropertyAddress());
				contactAustinEntity.setCommunity_name(validRecord.getLegalDescription());
				contactAustinEntity.setZip(zip);
				contactAustinEntity.setDetails_url(validRecord.getGeographicId());
				contactAustinEntity.setCreated_datetime(LocalDateTime.now());
				contactAustinEntity.setUser_id(Constant.USER_ID);
				contactRepository.save(contactAustinEntity);
				i++;

			} else {
				
				// Update Contact 
				
				contactAustinEntity.setName(validRecord.getOwnerName());
				contactAustinEntity.setProperty_address(validRecord.getPropertyAddress());
				contactAustinEntity.setCommunity_name(validRecord.getLegalDescription());
				contactAustinEntity.setZip(zip);
				contactAustinEntity.setUser_id(Constant.USER_ID);
				contactAustinEntity.setUpdated_datetime(LocalDateTime.now());
				contactRepository.save(contactAustinEntity);
				j++;
			}
			
			
		}

		logger.info("✅ Total records saved to DB  written to::: " + i);
		logger.info("✅ Total records updated to DB  written to::: " + j);

	}


	public boolean doesAddressMatch(String suitAddress, String ownerAddress) {

		if (suitAddress == null || suitAddress.equals("") || ownerAddress == null || ownerAddress.equals(""))
			return false;

		String suitAddressNor = normalizeForShortMatch(suitAddress);
		String ownerAddressNor = normalizeForShortMatch(ownerAddress);

		return shortAddressMatch(suitAddressNor, ownerAddressNor);

	}

	public String normalizeForShortMatch(String address) {
		if (address == null)
			return "";

		return address.toLowerCase().replaceAll("[^a-z0-9]", "") // remove all punctuation and spaces
				.substring(0, Math.min(10, address.length())); // take first 10 chars
	}

	public boolean shortAddressMatch(String addr1, String addr2) {
		String short1 = addr1;
		String short2 = addr2;

		// Check if either address prefix is contained in the other
		return short1.contains(short2) || short2.contains(short1);
	}
}