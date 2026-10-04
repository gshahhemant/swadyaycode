package com.swadyay.data.ai.sanantonio.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.swadyay.data.ai.sanantonio.services.CsvProcessor;


@RestController
@RequestMapping(value = "/ai")
public class AiDataSanAntonioCorpusController {

	@Autowired
	CsvProcessor csvProcessor;
	@Autowired
	private DownloadService downloadService;

	@GetMapping(value = "/testsanantonio")
	public ResponseEntity<String> test() {

		String str = "<html> <body><center><b>SWADHYAY AI s DATA APP  WORKING FINE   </b></center></body></html>";
		return new ResponseEntity<String>(str, HttpStatus.OK);
	}

	@GetMapping(value = "/process/sanantoniodata")
	public ResponseEntity<String> processSanantoniaSata(@RequestParam String fileName,@RequestParam String zipCode) {

		String str = "<html> <body><center><b>SANANTIONIO DATA PROCESSING FINISH </b></center></body></html>";
		
		str=str+":::"+fileName;

		csvProcessor.processCsv(fileName,zipCode);

		return new ResponseEntity<String>(str, HttpStatus.OK);

	}

	@PostMapping(value = "/process/sanantoniodata/upload")
	public ResponseEntity<String> processSanantoniaDataUpload(@RequestParam("file") MultipartFile file,
			@RequestParam String zipCode) throws IOException {

		long startTime = System.currentTimeMillis();

		String fileName = file.getOriginalFilename();
		if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
			fileName = fileName.substring(0, fileName.length() - 4);
		}

		Path targetPath = Paths.get("src/main/resources/" + fileName + ".csv");
		Files.createDirectories(targetPath.getParent());
		Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

		csvProcessor.processCsv(fileName, zipCode);

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
	
	@GetMapping(value = "/process/corpusdata")
	public ResponseEntity<String> processCorpusdata(@RequestParam String fileName,@RequestParam String zipCode) {

		String str = "<html> <body><center><b>CORPUS DATA PROCESSING FINISH </b></center></body></html>";
		
		str=str+":::"+fileName;

		csvProcessor.processCsvForCorpus(fileName,zipCode);

		return new ResponseEntity<String>(str, HttpStatus.OK);

	}
	
	@GetMapping(value = "/process/sanantoniodata/region")
	public ResponseEntity<String> processSantonioByRegion(@RequestParam String region,@RequestParam List<String> zipCodes) throws JsonMappingException, JsonProcessingException {

		String str = "<html> <body><center><b>SANANTIONIO Region Wise DATA PROCESSING FINISH </b></center></body></html>";
		
	

		csvProcessor.processNameByRegion(region, zipCodes);

		return new ResponseEntity<String>(str, HttpStatus.OK);

	}
	
	@GetMapping("/download")
    public ResponseEntity<String> startDownload() {
        downloadService.downloadAllPages();
        return ResponseEntity.ok("Download started. Check logs for progress.");
    }

}
