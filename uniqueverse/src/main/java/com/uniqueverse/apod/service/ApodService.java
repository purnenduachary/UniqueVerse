package com.uniqueverse.apod.service;

import com.uniqueverse.apod.client.NasaApodClient;
import com.uniqueverse.apod.dto.ApodResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApodService {

    private final NasaApodClient nasaApodClient;
    private final String apiKey;

    public ApodService(
            NasaApodClient nasaApodClient,
            @Value("${nasa.api-key}") String apiKey) {

        this.nasaApodClient = nasaApodClient;
        this.apiKey = apiKey;
    }

    public ApodResponse getTodayApod() {
        return nasaApodClient.getTodayApod(apiKey);
    }
}