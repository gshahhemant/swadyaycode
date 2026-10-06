import React from 'react';
import { API_ENDPOINTS } from '../config/api';
import { CountyDataUpload } from './CountyDataUpload';

export function AustinKendraUpload() {
  return (
    <CountyDataUpload
      title="Travis County Data Process (CSV or XLSX)"
      uploadEndpoint={API_ENDPOINTS.TRAVIS_COUNTY_DATA_UPLOAD}
      acceptedExtensions={['.csv', '.xlsx']}
    />
  );
}
