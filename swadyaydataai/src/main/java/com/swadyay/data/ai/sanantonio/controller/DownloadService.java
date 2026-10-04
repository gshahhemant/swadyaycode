package com.swadyay.data.ai.sanantonio.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class DownloadService {

    private final RestTemplate client = new RestTemplate();
    private final String urlTemplate = "https://esearch.nuecescad.net/search/SearchResultDownload?keywords=PropertyType%%3AReal%%20Year%%3A2026&isArb=false&pageNumber=%d&pageSize=%d";
    private final int totalRecords = 161908;
    private final Path outputDir = Paths.get("output");
    private String cookies = "";
    private final String searchUrl = "https://esearch.nuecescad.net/search/results";

    public void downloadAllPages() {
        try {
            Files.createDirectories(outputDir);

            int pageNumber = 1;
            int downloadedRecords = 0;
            
            // CRITICAL: Perform search first to establish session
            if (!performSearch()) {
                System.out.println("Failed to establish search session");
                return;
            }

            while (downloadedRecords < totalRecords) {
                int pageSize = pageNumber * 100;
                fetchPage(pageNumber, 500);
                downloadedRecords += pageSize;
                pageNumber++;
                Thread.sleep(500); // Rate limiting
            }

            System.out.println("All pages downloaded successfully. Total pages: " + (pageNumber - 1));
        } catch (Exception e) {
            System.out.println("Download failed: " + e.getMessage());
        }
    }
    private boolean performSearch() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            headers.set("Origin", "https://esearch.nuecescad.net");
            headers.set("Referer", "https://esearch.nuecescad.net/");
            
            String body = "keywords=PropertyType:Real Year:2026&isArb=false";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = client.postForEntity(searchUrl, entity, String.class);
            
            // Capture ALL cookies
            List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (setCookies != null && !setCookies.isEmpty()) {
                cookies = String.join("; ", setCookies);
                System.out.println("Session cookies captured: " + cookies);
                return true;
            }
            
            System.out.println("No cookies received from search");
            return false;
        } catch (Exception e) {
            System.out.println("Search failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    private void fetchPage(int pageNum, int pageSize) {
        String requestUrl = String.format(urlTemplate, pageNum, pageSize);
        
        System.out.println("Requesting: " + requestUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "*/*");
            headers.set("Accept-Encoding", "gzip, deflate, br");
            headers.set("Accept-Language", "en-US,en;q=0.9");
            headers.set("Connection", "keep-alive");
            headers.set("Host", "esearch.nuecescad.net");
            headers.set("Referer", "https://esearch.nuecescad.net/search/results");
            headers.set("Sec-Fetch-Dest", "empty");
            headers.set("Sec-Fetch-Mode", "cors");
            headers.set("Sec-Fetch-Site", "same-origin");
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> result = client.exchange(
                requestUrl, 
                HttpMethod.GET, 
                entity, 
                byte[].class
            );

            if (result.getBody() != null && result.getBody().length > 0) {
                Path file = outputDir.resolve("data_page_" + pageNum + ".csv");
                Files.write(file, result.getBody());
                
                String content = new String(result.getBody());
                int recordCount = content.split("\n").length - 1;
                System.out.println("Downloaded page: " + pageNum + " (" + recordCount + " records, " + result.getBody().length + " bytes)");
            }
        } catch (Exception ex) {
            System.out.println("Failed to download page " + pageNum + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}