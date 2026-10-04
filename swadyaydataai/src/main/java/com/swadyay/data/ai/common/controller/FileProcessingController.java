package com.swadyay.data.ai.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.swadyay.data.ai.common.dto.FileProcessLogResponse;
import com.swadyay.data.ai.common.service.FileProcessLogService;

@RestController
@RequestMapping(value = "/fileprocessing")
public class FileProcessingController {

	@Autowired
	FileProcessLogService fileProcessLogService;

	/**
	 * Returns all files that are currently in PROCESSING status,
	 * along with the elapsed time so far (hours/minutes/seconds).
	 */
	@GetMapping(value = "/processing")
	public ResponseEntity<List<FileProcessLogResponse>> getCurrentlyProcessingFiles() {

		List<FileProcessLogResponse> response = fileProcessLogService.getCurrentlyProcessingFiles();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * Returns file processing details (any status) started within the last N days,
	 * along with the total time taken (hours/minutes/seconds) for each. Defaults to 15 days.
	 */
	@GetMapping(value = "/history")
	public ResponseEntity<List<FileProcessLogResponse>> getProcessingHistory(
			@RequestParam(value = "days", required = false, defaultValue = "15") int days) {

		List<FileProcessLogResponse> response = fileProcessLogService.getStatusForLastDays(days);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
