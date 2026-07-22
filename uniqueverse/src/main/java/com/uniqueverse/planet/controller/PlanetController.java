package com.uniqueverse.planet.controller;

import com.uniqueverse.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlanetController {

    @GetMapping("/test")
    public String testException(){
        throw new ResourceNotFoundException("Planet not found");
    }
}
