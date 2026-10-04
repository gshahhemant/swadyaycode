import React from 'react';
import { API_ENDPOINTS } from '../config/api';
import { CountyDataUpload } from './CountyDataUpload';

export function CedarParkKendraUpload() {
  return (
    <CountyDataUpload title="Cedar Park Kendra Data Process (CSV only)" uploadEndpoint={API_ENDPOINTS.WILLIAMSON_COUNTY_DATA_UPLOAD} />
  );
}
