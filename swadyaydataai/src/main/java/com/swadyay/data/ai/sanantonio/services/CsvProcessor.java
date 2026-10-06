package com.swadyay.data.ai.sanantonio.services;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.swadyay.data.ai.austin.entity.ContactAustinEntity;
import com.swadyay.data.ai.austin.services.CsvProcessorWilliamsonCountyService;
import com.swadyay.data.ai.common.entity.FileProcessLog;
import com.swadyay.data.ai.common.service.FileProcessLogService;
import com.swadyay.data.ai.sanantonio.controller.Constant;

@Service
public class CsvProcessor {

	private static final Logger logger = LoggerFactory.getLogger(CsvProcessor.class);

	@Autowired
	AIService aIService;

	@Autowired
	ContactRepository contactRepository;

	@Autowired
	FileProcessLogService fileProcessLogService;

	private final String[] headers = { "Property ID", "Geographic ID", "Type", "Property Address", "Legal Description",
			"Owner Name", "Doing Business As", "Appraised Value" };

	public void processCsv(String inputFile, String zipCode) {
		
		int i = 1;
		int j = 1;
		String zip = zipCode;

		FileProcessLog fileProcessLog = fileProcessLogService.startLog(inputFile);

		String outputFile = "src/main/resources/" + inputFile + "_processed.csv";

		inputFile = "src/main/resources/" + inputFile + ".csv";

		List<PropertyRecord> validRecords = new ArrayList<>();

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
			fileProcessLogService.updateTotalRecords(fileProcessLog, cleanLines.size());
			boolean result = false;
			// ✅ Convert to POJO & filter
			for (String[] fields : cleanLines) {
				PropertyRecord record = new PropertyRecord();
				record.setPropertyId(fields[0]);
				record.setGeographicId(fields[1]);
				record.setPropertyAddress(fields[3]);
				record.setLegalDescription(fields[4]);
				record.setOwnerName(fields[5]);
				record.setDoingBusinessAs(fields[6]);
				
				//record.set
				
				

				if (record.getDoingBusinessAs() == null || record.getDoingBusinessAs().trim().isEmpty()) {
					
					logger.info("************ ownerName*****[" + i + "]::" + record.getOwnerName());

					result = aIService.isIndianName(fields[5]);

					logger.info("************result" + result);

					if (result) {
						
						logger.info("************Indian ownerName*****[" + j + "]::" + record.getOwnerName());

						validRecords.add(record);
						j++;
					}
				}
				logger.info("processed record[" + i + "] of total records [" + cleanLines.size() + "]");
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

		} catch (IOException e) {
			fileProcessLogService.failLog(fileProcessLog);
			throw new RuntimeException("Failed processing CSV", e);
		}
	}
	
	public void processCsvForCorpus(String inputFile, String zipcode) {

		logger.info("************** Start processCsvForCorpus *****************");

		int i = 1;
		int j = 1;

		String zip = zipcode;

		FileProcessLog fileProcessLog = fileProcessLogService.startLog(inputFile);

		String outputFile = "src/main/resources/" + inputFile + "_processed.csv";

		inputFile = "src/main/resources/" + inputFile + ".csv";

		List<PropertyRecord> validRecords = new ArrayList<>();

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
			fileProcessLogService.updateTotalRecords(fileProcessLog, cleanLines.size());
			boolean result = false;
			// ✅ Convert to POJO & filter
			for (String[] fields : cleanLines) {

				if (fields[3] != null) {

					PropertyRecord record = new PropertyRecord();
					record.setPropertyId(fields[0]);
					record.setOwnerName(fields[1]);
					record.setDoingBusinessAs(fields[2]);
					record.setPropertyAddress(fields[3]);

					String zipCode = getZip(record.getPropertyAddress());
					
					record.setZip(zipCode);

					logger.info("************ Zip*** " + zipCode);

					logger.info("************ Property info*****[" + i + "]::" + record.toString());

					if (record.getDoingBusinessAs() == null || record.getDoingBusinessAs().trim().isEmpty()) {

						logger.info("************ ownerName*****[" + i + "]::" + record.getOwnerName());

						result = aIService.isIndianName(record.getOwnerName());

						logger.info("************result" + result);

						if (result) {

							logger.info("************Indian ownerName*****[" + j + "]::" + record.getOwnerName());

							validRecords.add(record);
							j++;
						}
					}

					logger.info("processed record[" + i + "] of total records [" + cleanLines.size() + "]");
					i++;
				}else {
					logger.info("************ Address is null*** ");
				}
			}

			logger.info("total Indian names for  :::" + validRecords.size());

			// ✅ Sort
	//		validRecords = validRecords.stream().sorted(
		//			Comparator.comparing(PropertyRecord::getGeographicId, Comparator.nullsLast(String::compareTo)))
			//		.collect(Collectors.toList());

			// ✅ Write output
			try (CSVWriter writer = new CSVWriter(new FileWriter(outputFile))) {
				writer.writeNext(headers);
				for (PropertyRecord record : validRecords) {
					writer.writeNext(new String[] { record.getPropertyId(),  record.getOwnerName(), 
							record.getPropertyAddress(),record.getDoingBusinessAs() });
				}
			}

			logger.info("✅ Output written to: " + outputFile);

			dumpDataToDatabase(validRecords, zip);

			fileProcessLogService.completeLog(fileProcessLog, cleanLines.size());

			logger.info("************** END processCsvForCorpus *****************");

		} catch (IOException e) {
			fileProcessLogService.failLog(fileProcessLog);
			throw new RuntimeException("Failed processing CSV", e);
		}
	}

	public String getZip (String address) {
		
		 // Extract ZIP code
        Pattern zipPattern = Pattern.compile("\\b(\\d{5})(?:-\\d{4})?\\b");
        Matcher zipMatcher = zipPattern.matcher(address);
        if (zipMatcher.find()) {
           return zipMatcher.group(1);
        }
        return null;
	}
	
	
	
	public void dumpDataToDatabase(List<PropertyRecord> validRecords, String zip) {
	    int i = 0;
	    int j = 0;
	    for (PropertyRecord validRecord : validRecords) {

	        String propertyId = validRecord.getPropertyId();

	        Contact contact = contactRepository.findById(new ContactSanAntonioId(propertyId, zip)).orElse(null);

	        // not found as-is; if id has an "R"/"r" prefix, retry with the id stripped
	        if (contact == null && propertyId != null
	                && (propertyId.startsWith("R") || propertyId.startsWith("r"))) {
	            contact = contactRepository.findById(new ContactSanAntonioId(propertyId.substring(1), zip)).orElse(null);
	        }

	        if (contact == null) {

	            contact = new Contact();
	            contact.setProperty_id(propertyId);
	            contact.setName(validRecord.getOwnerName());
	            contact.setProperty_address(validRecord.getPropertyAddress());
	            contact.setCommunity_name(validRecord.getLegalDescription());
	            contact.setDetails_url(validRecord.getGeographicId());
	            contact.setZip(zip);
	            contact.setCreated_datetime(LocalDateTime.now());
	            contact.setUser_id(Constant.USER_ID);
	            contactRepository.save(contact);
	            i++;

	        } else {

	            // Update Contact

	            contact.setName(validRecord.getOwnerName());
	            contact.setProperty_address(validRecord.getPropertyAddress());
	            contact.setCommunity_name(validRecord.getLegalDescription());
	            contact.setDetails_url(validRecord.getGeographicId());
	            contact.setZip(zip);
	            contact.setUpdated_datetime(LocalDateTime.now());
	            contact.setUser_id(Constant.USER_ID);
	            contactRepository.save(contact);
	            j++;
	        }

	    }

	    logger.info("✅ Total records saved to DB  written to::: " + i);
	    logger.info("✅ Total records updated to DB  written to::: " + j);

	}
	public void processNameByRegion(String rigion, List<String>  zips) throws JsonMappingException, JsonProcessingException {
		
		boolean result = false;
		
		 LinkedList<Contact> contacts = contactRepository.filterContactByZips(zips);
		 int j=1;
		 int i=1;
		 
		 for(Contact contact: contacts) {
			 
			 result= aIService.isIndianRegionName(rigion,contact.getName());
			 

				if (result) {

					logger.info("************Indian ownerName "+rigion+" *****[" + j + "]::" + contact.getName());

					contact.setHome_stead(Constant.MARATHI);
					contact.setUpdated_datetime(LocalDateTime.now());
					contact.setUser_id(Constant.USER_ID);
					contactRepository.save(contact);
					j++;
				}
				
				logger.info("processed record[" + i + "] of total records [" + contacts.size() + "]");
			 i++;
		 }
		
	}

}
