package ru.gypsyjr.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gypsyjr.models.Statistic;
import ru.gypsyjr.service.ApiService;

@RestController
public class ApiController {
    @Autowired
    ApiService apiService;

    @GetMapping("/statistics")
    public ResponseEntity<Statistic> getStatistics() {
        return ResponseEntity.ok().body(apiService.getStatistic());
    }
}
