package com.swadyay.data.ai.common.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swadyay.data.ai.common.entity.FileProcessLog;

@Repository
public interface FileProcessLogRepository extends JpaRepository<FileProcessLog, Long> {

	List<FileProcessLog> findByStatus(String status);

	List<FileProcessLog> findByStartTimeAfterOrderByStartTimeDesc(LocalDateTime since);
}
