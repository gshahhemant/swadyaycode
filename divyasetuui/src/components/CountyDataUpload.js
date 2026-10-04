import React, { useState, useRef } from 'react';
import { useNotification, Notification } from './useNotification';
import { FileProcessingStatus } from './FileProcessingStatus';

export function CountyDataUpload({ title, uploadEndpoint, acceptedExtensions = ['.csv'], children }) {
  const [file, setFile] = useState(null);
  const [zipCode, setZipCode] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadPercent, setUploadPercent] = useState(0);
  const [phase, setPhase] = useState(null); // 'uploading' | 'processing'
  const [result, setResult] = useState(null);
  const { notification, showNotification, hideNotification } = useNotification();
  const statusRef = useRef(null);

  const handleFileChange = (e) => {
    const selected = e.target.files?.[0] || null;
    if (selected && !acceptedExtensions.some(ext => selected.name.toLowerCase().endsWith(ext))) {
      showNotification(`Only ${acceptedExtensions.join(', ').toUpperCase()} files are allowed.`, 'error');
      e.target.value = '';
      setFile(null);
      return;
    }
    setFile(selected);
    setResult(null);
  };

  const handleUpload = () => {
    if (!file) {
      showNotification(`Please select a ${acceptedExtensions.join('/').toUpperCase()} file to upload.`, 'error');
      return;
    }
    if (!zipCode.trim()) {
      showNotification('Please enter a Zip Code.', 'error');
      return;
    }

    setUploading(true);
    setUploadPercent(0);
    setPhase('uploading');
    setResult(null);

    const formData = new FormData();
    formData.append('file', file);

    const url = `${uploadEndpoint}?zipCode=${encodeURIComponent(zipCode.trim())}`;
    const xhr = new XMLHttpRequest();
    xhr.open('POST', url);

    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) {
        setUploadPercent(Math.round((e.loaded / e.total) * 100));
      }
    };
    xhr.upload.onload = () => {
      // file fully sent; server now processes it with no progress feedback available
      setPhase('processing');
    };

    xhr.onload = () => {
      setUploading(false);
      setPhase(null);
      const text = xhr.responseText;
      if (xhr.status >= 200 && xhr.status < 300) {
        setResult(text);
        showNotification(text || 'Processed successfully.', 'success');
        statusRef.current?.refresh();
      } else {
        showNotification(text || 'Upload failed', 'error');
      }
    };

    xhr.onerror = () => {
      setUploading(false);
      setPhase(null);
      showNotification('Upload failed', 'error');
    };

    xhr.send(formData);
  };

  return (
    <>
      <Notification notification={notification} onClose={hideNotification} />
      <div className="section-title">{title}</div>
      <div style={{ display: 'flex', gap: '18px', marginBottom: '18px', flexWrap: 'wrap', alignItems: 'center' }}>
        <input type="file" accept={acceptedExtensions.join(',')} onChange={handleFileChange} />
        <input
          type="text"
          placeholder="Zip Code"
          value={zipCode}
          onChange={(e) => setZipCode(e.target.value)}
        />
        <button onClick={handleUpload} disabled={uploading}>
          {uploading ? 'Processing...' : 'Process Data'}
        </button>
      </div>
      {uploading && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '18px' }}>
          <div className="progress-bar-track">
            <div
              className={`progress-bar-fill${phase === 'processing' ? ' indeterminate' : ''}`}
              style={phase === 'processing' ? undefined : { width: `${uploadPercent}%` }}
            />
          </div>
          <span style={{ fontSize: '13px', color: '#718096' }}>
            {phase === 'processing' ? 'Processing on server...' : `Uploading ${uploadPercent}%`}
          </span>
        </div>
      )}
      {result && <div style={{ color: 'green' }}>{result}</div>}

      {children}

      <FileProcessingStatus ref={statusRef} showNotification={showNotification} />
    </>
  );
}
