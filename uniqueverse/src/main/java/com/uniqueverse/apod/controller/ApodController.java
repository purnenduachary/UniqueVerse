package com.uniqueverse.apod.controller;

import com.uniqueverse.apod.dto.ApodResponse;
import com.uniqueverse.apod.service.ApodService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/apod")
public class ApodController {

    private final ApodService apodService;

    public ApodController(ApodService apodService) {
        this.apodService = apodService;
    }

    @GetMapping
    public ApodResponse getTodayApod() {
        return apodService.getTodayApod();
    }
}