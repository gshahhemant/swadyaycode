import React, { useState, useEffect, useCallback, forwardRef, useImperativeHandle } from 'react';
import { API_ENDPOINTS } from '../config/api';
import '../styles/FileProcessingTables.css';

const formatDateTime = (value) => {
  if (!value) return '-';
  const date = new Date(value);
  return isNaN(date.getTime()) ? value : date.toLocaleString();
};

const formatDuration = (row) => {
  if (row.durationFormatted) return row.durationFormatted;
  const h = row.durationHours || 0;
  const m = row.durationMinutes || 0;
  const s = row.durationSeconds || 0;
  return `${h}h ${m}m ${s}s`;
};

const statusClass = (status) => {
  const key = (status || '').toLowerCase();
  if (key.includes('pending')) return 'status-pending';
  if (key.includes('complet')) return 'status-completed';
  if (key.includes('fail') || key.includes('error')) return 'status-failed';
  if (key.includes('process')) return 'status-processing';
  return 'status-other';
};

function FileProcessingTable({ title, rows }) {
  return (
    <div className="file-processing-table-wrapper">
      {title && <div className="section-title" style={{ fontSize: '20px' }}>{title}</div>}
      {rows.length === 0 ? (
        <div className="file-processing-empty">No records found.</div>
      ) : (
        <table className="file-processing-table">
          <thead>
            <tr>
              <th>File Name</th>
              <th>Start Time</th>
              <th>End Time</th>
              <th>Total Duration</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row, idx) => (
              <tr key={row.srNo ?? idx}>
                <td>{row.fileName}</td>
                <td>{formatDateTime(row.startTime)}</td>
                <td>{formatDateTime(row.endTime)}</td>
                <td>{formatDuration(row)}</td>
                <td>
                  <span className={`status-badge ${statusClass(row.status)}`}>{row.status}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

export const FileProcessingStatus = forwardRef(function FileProcessingStatus({ showNotification }, ref) {
  const [pendingRows, setPendingRows] = useState([]);
  const [otherRows, setOtherRows] = useState([]);
  const [loadingFiles, setLoadingFiles] = useState(false);
  const [historyDays, setHistoryDays] = useState('15');

  const fetchFileProcessingData = useCallback(async (days) => {
    setLoadingFiles(true);
    try {
      const daysParam = days ?? historyDays ?? '15';
      const [processingRes, historyRes] = await Promise.all([
        fetch(API_ENDPOINTS.FILE_PROCESSING_CURRENT),
        fetch(`${API_ENDPOINTS.FILE_PROCESSING_HISTORY}?days=${encodeURIComponent(daysParam)}`)
      ]);
      const processingData = processingRes.ok ? await processingRes.json() : [];
      const historyData = historyRes.ok ? await historyRes.json() : [];

      const processingKeys = new Set(
        processingData.map((row) => row.srNo ?? `${row.fileName}-${row.startTime}`)
      );
      const merged = new Map();
      [...processingData, ...historyData].forEach((row) => {
        merged.set(row.srNo ?? `${row.fileName}-${row.startTime}`, row);
      });
      const allRows = Array.from(merged.values());

      const isActive = (row) =>
        processingKeys.has(row.srNo ?? `${row.fileName}-${row.startTime}`) ||
        (row.status || '').toLowerCase().includes('processing') ||
        (row.status || '').toLowerCase().includes('pending');

      setPendingRows(allRows.filter(isActive));
      setOtherRows(allRows.filter((row) => !isActive(row)));
    } catch (error) {
      showNotification?.('Failed to load file processing data.', 'error');
    } finally {
      setLoadingFiles(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useImperativeHandle(ref, () => ({
    refresh: (days) => fetchFileProcessingData(days)
  }));

  useEffect(() => {
    fetchFileProcessingData(historyDays);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="file-processing-section">
      <div className="file-processing-header">
        <div className="section-title">File Processing Status</div>
        <button
          className="file-processing-refresh-btn"
          onClick={() => fetchFileProcessingData(historyDays)}
          disabled={loadingFiles}
        >
          {loadingFiles ? 'Refreshing...' : 'Refresh'}
        </button>
      </div>
      <FileProcessingTable title="Pending" rows={pendingRows} />

      <div className="file-processing-header">
        <div className="section-title" style={{ fontSize: '20px' }}>Processed / Other Status</div>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <label htmlFor="historyDays" style={{ fontSize: '14px', color: '#333' }}>Days:</label>
          <input
            id="historyDays"
            type="text"
            value={historyDays}
            onChange={(e) => setHistoryDays(e.target.value)}
            placeholder="Days"
            style={{ width: '70px' }}
          />
          <button
            className="file-processing-refresh-btn"
            onClick={() => fetchFileProcessingData(historyDays)}
            disabled={loadingFiles}
          >
            {loadingFiles ? 'Loading...' : 'Load'}
          </button>
        </div>
      </div>
      <FileProcessingTable title="" rows={otherRows} />
    </div>
  );
});
