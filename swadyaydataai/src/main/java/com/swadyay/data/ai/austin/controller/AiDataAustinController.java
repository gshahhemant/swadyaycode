package com.swadyay.data.ai.austin.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.swadyay.data.ai.austin.services.CsvProcessorTravisCountyService;
import com.swadyay.data.ai.austin.services.CsvProcessorWilliamsonCountyService;




@RestController
@RequestMapping(value = "/ai")
public class AiDataAustinController {

	@Autowired
	CsvProcessorTravisCountyService csvProcessor;

	@Autowired
	CsvProcessorWilliamsonCountyService csvProcessorWilliamsonCounty;
	
	@GetMapping(value = "/test")
	public ResponseEntity<String> test() {

		String str = "<html> <body><center><b>SWADHYAY Austin AI s DATA APP  WORKING FINE   </b></center></body></html>";
		return new ResponseEntity<String>(str, HttpStatus.OK);
	}

	@GetMapping(value = "/process/traviscountydata")
	public ResponseEntity<String> processTravisCountyData(@RequestParam String fileName,@RequestParam String zipCode) {

		String str = "<html> <body><center><b>AUSTIN TRAVISCOUNTY DATA PROCESSING FINISH </b></center></body></html>";
		
		str=str+":::"+fileName;

		csvProcessor.processCsv(fileName,zipCode);

		return new ResponseEntity<String>(str, HttpStatus.OK);

	}

	@PostMapping(value = "/process/traviscountydata/upload")
	public ResponseEntity<String> processTravisCountyDataUpload(@RequestParam("file") MultipartFile file,
			@RequestParam String zipCode) throws IOException {

		long startTime = System.currentTimeMillis();

		String fileName = file.getOriginalFilename();
		String baseName = fileName;
		String extension = ".csv";

		if (fileName != null) {
			String lowerFileName = fileName.toLowerCase(Locale.ROOT);
			if (lowerFileName.endsWith(".xlsx") || lowerFileName.endsWith(".xls") || lowerFileName.endsWith(".csv")) {
				int dotIndex = fileName.lastIndexOf('.');
				baseName = fileName.substring(0, dotIndex);
				extension = fileName.substring(dotIndex);
			}
		}

		// Travis County processing supports both CSV and Excel (.xlsx/.xls) input files,
		// so preserve whichever extension was uploaded.
		Path targetPath = Paths.get("src/main/resources/" + baseName + extension);
		Files.createDirectories(targetPath.getParent());
		Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

		csvProcessor.processCsv(baseName, zipCode);

		long elapsedMillis = System.currentTimeMillis() - startTime;
		String duration = formatDuration(elapsedMillis);

		return new ResponseEntity<String>("Success - Time taken: " + duration, HttpStatus.OK);
	}
	
	@GetMapping(value = "/process/williamsoncountydata")
	public ResponseEntity<String> processWilliamsonCountyData(@RequestParam String fileName,@RequestParam String zipCode) {

		String str = "<html> <body><center><b>AUSTIN WILLIAMSON COUNTY PROCESSING FINISH </b></center></body></html>";
		
		str=str+":::"+fileName;

		csvProcessorWilliamsonCounty.processCsv(fileName,zipCode);

		return new ResponseEntity<String>(str, HttpStatus.OK);

	}

	@PostMapping(value = "/process/williamsoncountydata/upload")
	public ResponseEntity<String> processWilliamsonCountyDataUpload(@RequestParam("file") MultipartFile file,
			@RequestParam String zipCode) throws IOException {

		long startTime = System.currentTimeMillis();

		String fileName = file.getOriginalFilename();
		if (fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".csv")) {
			fileName = fileName.substring(0, fileName.length() - 4);
		}

		Path targetPath = Paths.get("src/main/resources/" + fileName + ".csv");
		Files.createDirectories(targetPath.getParent());
		Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

		csvProcessorWilliamsonCounty.processCsv(fileName, zipCode);

		long elapsedMillis = System.currentTimeMillis() - startTime;
		String duration = formatDuration(elapsedMillis);

		return new ResponseEntity<String>("Success - Time taken: " + duration, HttpStatus.OK);
	}

	private String formatDuration(long millis) {
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
		return hours + " hours " + minutes + " minutes " + seconds + " seconds";
	}


}

