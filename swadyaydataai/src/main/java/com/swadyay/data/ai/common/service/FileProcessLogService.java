package com.swadyay.data.ai.common.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swadyay.data.ai.common.dto.FileProcessLogResponse;
import com.swadyay.data.ai.common.entity.FileProcessLog;
import com.swadyay.data.ai.common.repository.FileProcessLogRepository;

@Service
public class FileProcessLogService {

    @Autowired
    FileProcessLogRepository fileProcessLogRepository;

    /**
     * Creates and persists a new log entry with status PROCESSING for the given file.
     */
    public FileProcessLog startLog(String fileName) {
        FileProcessLog fileProcessLog = new FileProcessLog();
        fileProcessLog.setFileName(fileName);
        fileProcessLog.setStartTime(LocalDateTime.now());
        fileProcessLog.setStatus(FileProcessLog.STATUS_PROCESSING);
        return fileProcessLogRepository.save(fileProcessLog);
    }

    /**
     * Updates the in-progress log entry with the total number of (cleaned) records found,
     * so the record count is visible while the file is still PROCESSING.
     */
    public void updateTotalRecords(FileProcessLog fileProcessLog, int totalRecords) {
        fileProcessLog.setNumberOfRecords(totalRecords);
        fileProcessLogRepository.save(fileProcessLog);
    }

    /**
     * Marks the log entry as COMPLETED, recording the end time and number of records processed.
     */
    public void completeLog(FileProcessLog fileProcessLog, int numberOfRecords) {
        fileProcessLog.setStatus(FileProcessLog.STATUS_COMPLETED);
        fileProcessLog.setNumberOfRecords(numberOfRecords);
        fileProcessLog.setEndTime(LocalDateTime.now());
        fileProcessLogRepository.save(fileProcessLog);
    }

    /**
     * Marks the log entry as FAILED, recording the end time.
     */
    public void failLog(FileProcessLog fileProcessLog) {
        fileProcessLog.setStatus(FileProcessLog.STATUS_FAILED);
        fileProcessLog.setEndTime(LocalDateTime.now());
        fileProcessLogRepository.save(fileProcessLog);
    }

    /**
     * Returns all files currently in PROCESSING status, with elapsed duration so far.
     */
    public List<FileProcessLogResponse> getCurrentlyProcessingFiles() {
        return fileProcessLogRepository.findByStatus(FileProcessLog.STATUS_PROCESSING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns all file processing records (any status) started within the last given number of days.
     */
    public List<FileProcessLogResponse> getStatusForLastDays(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return fileProcessLogRepository.findByStartTimeAfterOrderByStartTimeDesc(since).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private FileProcessLogResponse toResponse(FileProcessLog fileProcessLog) {
        FileProcessLogResponse response = new FileProcessLogResponse();
        response.setSrNo(fileProcessLog.getSrNo());
        response.setFileName(fileProcessLog.getFileName());
        response.setStartTime(fileProcessLog.getStartTime());
        response.setEndTime(fileProcessLog.getEndTime());
        response.setStatus(fileProcessLog.getStatus());
        response.setNumberOfRecords(fileProcessLog.getNumberOfRecords());

        if (fileProcessLog.getStartTime() != null) {
            LocalDateTime effectiveEnd = fileProcessLog.getEndTime() != null
                    ? fileProcessLog.getEndTime()
                    : LocalDateTime.now();

            Duration duration = Duration.between(fileProcessLog.getStartTime(), effectiveEnd);
            long totalSeconds = Math.max(duration.getSeconds(), 0);

            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;

            response.setDurationHours(hours);
            response.setDurationMinutes(minutes);
            response.setDurationSeconds(seconds);
            response.setDurationFormatted(String.format("%02d hour(s), %02d minute(s), %02d second(s)", hours, minutes, seconds));
        }

        return response;
    }
}
