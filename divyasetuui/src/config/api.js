// API Configuration
//export const API_BASE_URL = 'http://localhost:8080';
export const API_BASE_URL = 'http://192.168.0.250:8080';
//export const API_BASE_URL_AI_APP = 'http://localhost:8081';
export const API_BASE_URL_AI_APP = 'http://192.168.0.250:8081';

// API Endpoints
export const API_ENDPOINTS = {
  BASE: API_BASE_URL,
  DIVYASETU: `${API_BASE_URL}/divyasetu`,
  CONTACT_CHATBOT_BASE: 'http://localhost:8085/angeticai',
  SANANTONIO_DATA_UPLOAD: `${API_BASE_URL_AI_APP}/divyasetu-ai/ai/process/sanantoniodata/upload`,
  SANANTONIO_RETRIEVE_FOLDER: `${API_BASE_URL}/divyasetu/api/sanantonio/folder/zip`,
  TRAVIS_COUNTY_DATA_UPLOAD: `${API_BASE_URL_AI_APP}/divyasetu-ai/ai/process/traviscountydata/upload`,
  WILLIAMSON_COUNTY_DATA_UPLOAD: `${API_BASE_URL_AI_APP}/divyasetu-ai/ai/process/williamsoncountydata/upload`,
  FILE_PROCESSING_CURRENT: `${API_BASE_URL_AI_APP}/divyasetu-ai/fileprocessing/processing`,
  FILE_PROCESSING_HISTORY: `${API_BASE_URL_AI_APP}/divyasetu-ai/fileprocessing/history`,
  
  // Auth endpoints
  LOGIN: `${API_BASE_URL}/divyasetu/login/validateuser`,
  
  // Data endpoints
  ZIP_AND_CITY: `${API_BASE_URL}/divyasetu/api/zipandcity`,
  COMMUNITIES: `${API_BASE_URL}/divyasetu/api/communities`,
  ALL_COMMUNITIES: `${API_BASE_URL}/divyasetu/api/communities`,
  CONTACTS: `${API_BASE_URL}/divyasetu/api/contacts`,
  ADD_CONTACT: `${API_BASE_URL}/divyasetu/api/addcontact`,
  UPDATE_CONTACTS: `${API_BASE_URL}/divyasetu/api/updatecontacts`,
  DOWNLOAD: `${API_BASE_URL}/divyasetu/api/download`,
  DOWNLOAD_COMMUNITY_EXCEL: `${API_BASE_URL}/divyasetu/api/folder/zip`,
  CONTACT_CHATBOT_SEARCH: 'http://192.168.0.250:8085/angeticai/api/pdf/search',
  VISHAR_AND_ZIP: `${API_BASE_URL}/divyasetu/api/vishar`,
  KENDRA_NAMES: `${API_BASE_URL}/divyasetu/api/communities/kendranames`
};

// Helper function to build dynamic URLs
export const buildApiUrl = (endpoint, params = {}) => {
  let url = endpoint;
  Object.keys(params).forEach(key => {
    url = url.replace(`{${key}}`, params[key]);
  });
  return url;
};
