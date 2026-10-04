import React, { useState } from 'react';
import { API_ENDPOINTS } from '../config/api';
import { useNotification, Notification } from './useNotification';
import { CountyDataUpload } from './CountyDataUpload';

export function SanAntonioUpload() {
  const [retrieveZip, setRetrieveZip] = useState('');
  const [commentYear, setCommentYear] = useState('2026');
  const [retrieving, setRetrieving] = useState(false);
  const { notification, showNotification, hideNotification } = useNotification();

  const handleRetrieveFolder = () => {
    if (!retrieveZip.trim()) {
      showNotification('Please enter a Zip Code.', 'error');
      return;
    }
    if (!commentYear.trim()) {
      showNotification('Please enter a Comment Year.', 'error');
      return;
    }

    setRetrieving(true);
    const url = `${API_ENDPOINTS.SANANTONIO_RETRIEVE_FOLDER}/${encodeURIComponent(retrieveZip.trim())}?commentYear=${encodeURIComponent(commentYear.trim())}`;
    fetch(url)
      .then(response => {
        if (!response.ok) throw new Error('Failed to retrieve folder.');
        return response.blob();
      })
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${retrieveZip.trim()}.xlsx`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
      })
      .catch(err => {
        showNotification(err.message || 'Retrieve folder failed', 'error');
      })
      .finally(() => {
        setRetrieving(false);
      });
  };

  return (
    <CountyDataUpload title="San Antonio Data Process (CSV only)" uploadEndpoint={API_ENDPOINTS.SANANTONIO_DATA_UPLOAD}>
      <Notification notification={notification} onClose={hideNotification} />
      <div className="section-title">Retrieve San Antonio Folder</div>
      <div style={{ display: 'flex', gap: '18px', marginBottom: '18px', flexWrap: 'wrap', alignItems: 'center' }}>
        <input
          type="text"
          placeholder="Zip Code"
          value={retrieveZip}
          onChange={(e) => setRetrieveZip(e.target.value)}
        />
        <input
          type="text"
          placeholder="Comment Year"
          value={commentYear}
          onChange={(e) => setCommentYear(e.target.value)}
        />
        <button onClick={handleRetrieveFolder} disabled={retrieving}>
          {retrieving ? 'Retrieving...' : 'Retrieve Folder'}
        </button>
      </div>
    </CountyDataUpload>
  );
}
