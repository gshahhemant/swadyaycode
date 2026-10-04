package com.swadyay.data.ai.austin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OllamaAIService {

	private final WebClient webClient;

	public OllamaAIService() {
		this.webClient = WebClient.builder().baseUrl("http://localhost:11434")
				
				.clientConnector(
						new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(60)) // response
																													// timeout
								.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // connect timeout
						))
				.filter(logRequest())
                .filter(logResponse())
				.build();
	}

	public boolean isIndianName(String name) throws JsonMappingException, JsonProcessingException {
		try {
		String response =restCallForIndianName(name);
		ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        String answer = root.path("response").asText().trim().toLowerCase();
        
        return answer.contains("yes");
        
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	public String restCallForIndianName(String name) {
		
		String prompt = "Is the name " + name + " an Indian Hindu name? Just say Yes or No.";

        String requestBody = """
        {
          "model": "gemma3:12b",
          "prompt": "%s",
           "stream": false
         }
        """.formatted(prompt);
        
        System.out.println();

        return webClient.post()
        		.uri("/api/generate") 
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class).block();
    }

	
	 private ExchangeFilterFunction logRequest() {
	        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
	            System.out.println("➡️ Request: " + clientRequest.method() + " " + clientRequest.url());
	              return Mono.just(clientRequest);
	        });
	    }

	    private ExchangeFilterFunction logResponse() {
	        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
	            System.out.println("⬅️ Response Status: " + clientResponse.statusCode());
	           
	            return Mono.just(clientResponse);
	        });
	    }
}
