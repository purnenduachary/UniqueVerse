package com.uniqueverse.apod.client;

import com.uniqueverse.apod.dto.ApodResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NasaApodClient {

    private final RestClient restClient;

    public NasaApodClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.nasa.gov")
                .build();
    }

    public ApodResponse getTodayApod(String apiKey) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/planetary/apod")
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .body(ApodResponse.class);
    }
}