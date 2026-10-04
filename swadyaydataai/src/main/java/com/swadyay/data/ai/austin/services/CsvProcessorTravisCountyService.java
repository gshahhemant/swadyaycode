package com.swadyay.data.ai.austin.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
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
public class CsvProcessorTravisCountyService {
	
	 private static final Logger logger = LoggerFactory.getLogger(CsvProcessorTravisCountyService.class);

	@Autowired
	OllamaAIService openAIService;
	
	public final String REAL_PROPERTY="R";

	@Autowired
	ContactAustinRepository contactRepository;

	@Autowired
	FileProcessLogService fileProcessLogService;

	private final String[] headers = { "Property ID", "Geographic ID", "Type", "Property Address", "Legal Description",
			"Owner Name", "Doing Business As", "Appraised Value" };

	public void processCsv(String inputFile,String zipCode) {
		
		
	    LocalDateTime start = LocalDateTime.now();
		
		logger.info("process started TIME::: "+LocalDateTime.now());

		FileProcessLog fileProcessLog = fileProcessLogService.startLog(inputFile);

		String zip = zipCode;

		// strip a passed-in .xlsx/.xls extension so callers can pass either a base name or an excel file name
		String baseName = inputFile;
		if (baseName.toLowerCase(Locale.ROOT).endsWith(".xlsx") || baseName.toLowerCase(Locale.ROOT).endsWith(".xls")) {
			baseName = baseName.substring(0, baseName.lastIndexOf('.'));
		}

		String outputFile = "src/main/resources/" + baseName + "_processed.csv";
		String csvFile = "src/main/resources/" + baseName + ".csv";
		String xlsxFile = "src/main/resources/" + baseName + ".xlsx";

		try {
			if (Files.exists(Paths.get(xlsxFile))) {
				logger.info("Excel file detected, converting to CSV: " + xlsxFile);
				convertExcelToCsv(xlsxFile, csvFile);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed converting Excel file to CSV", e);
		}

		inputFile = csvFile;

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
			boolean result = false;
			// ✅ Convert to POJO & filter
			int i = 1;
			int j=1;
			for (String[] fields : cleanLines) {
			
				PropertyRecord record = new PropertyRecord();
				record.setPropertyId(fields[2]);
				record.setType(fields[3]);
				record.setGeographicId(fields[4]);
				record.setOwnerName(fields[7]);
				record.setPropertyAddress(fields[10]);
				record.setCity(fields[11]);
				record.setLegalDescription(fields[12]);
				
				/* Append city with Address if not already present */
				String propertyAddress = fields[10] == null ? "" : fields[10].trim();
				String city = fields[11] == null ? "" : fields[11].trim();

				if (!city.isEmpty()
				        && !propertyAddress.toLowerCase(Locale.ROOT)
				                .contains(city.toLowerCase(Locale.ROOT))) {
				    propertyAddress = propertyAddress + ", " + city;
				}

				record.setPropertyAddress(propertyAddress);
				record.setCity(city);
				
				/* END */
				
				
				logger.info("************ownerName*****" + record.getOwnerName()+" propertyID::"+record.getPropertyId());
				
				//check for REAL property
				if (record.getType() != null && record.getType().equalsIgnoreCase(REAL_PROPERTY)) {
					
					logger.info("REAL Property"+record.getType());

					result = openAIService.isIndianName(record.getOwnerName());

					logger.info("************result" + result);

					if (result) {

						logger.info("************Indian ownerName*****[" + j + "]::" + record.getOwnerName());
						validRecords.add(record);

					}

				}else {
					
					logger.info("************ownerName*****" + record.getOwnerName()+" propertyID::"+record.getPropertyId()+" propertyType::"+record.getType());
				}
				logger.info("processed record[" + i + "] of total records [" + cleanLines.size() + "]");
				
				i++;
				j++;
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

		 dumpDataToDatabase(validRecords,zip);

		 fileProcessLogService.completeLog(fileProcessLog, validRecords.size());

		} catch (IOException e) {
			fileProcessLogService.failLog(fileProcessLog);
			throw new RuntimeException("Failed processing CSV", e);
		}
		
		logger.info("process complated  TIME::: "+LocalDateTime.now());
		
		  LocalDateTime end = LocalDateTime.now();
		  
		  // Calculate duration
	        Duration duration = Duration.between(start, end);
	        long totalSeconds = duration.getSeconds();
	        
	        long hours = totalSeconds / 3600;
	        long minutes = (totalSeconds % 3600) / 60;
	        long seconds = totalSeconds % 60;

	        // Print nicely
	        logger.info(String.format("⏱️ Process took: %02d hour(s), %02d minute(s), %02d second(s)", hours, minutes, seconds));
		
	}
	
	private void convertExcelToCsv(String xlsxFilePath, String csvFilePath) throws IOException {

		try (Workbook workbook = WorkbookFactory.create(new File(xlsxFilePath));
				CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {

			Sheet sheet = workbook.getSheetAt(0);
			DataFormatter formatter = new DataFormatter();

			for (Row row : sheet) {
				int lastCellNum = Math.max(row.getLastCellNum(), 0);
				String[] values = new String[lastCellNum];

				for (int c = 0; c < lastCellNum; c++) {
					Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					values[c] = formatter.formatCellValue(cell);
				}

				writer.writeNext(values);
			}
		}

		logger.info("✅ Converted Excel file to CSV: " + csvFilePath);
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
				contactAustinEntity.setDetails_url(validRecord.getGeographicId());
				contactAustinEntity.setUpdated_datetime(LocalDateTime.now());
				contactAustinEntity.setUser_id(Constant.USER_ID);
				contactRepository.save(contactAustinEntity);
				j++;
			}
			
			
		}

		logger.info("✅ Total records saved to DB  written to::: " + i);
		logger.info("✅ Total records updated to DB  written to::: " + j);

	}

	
}
