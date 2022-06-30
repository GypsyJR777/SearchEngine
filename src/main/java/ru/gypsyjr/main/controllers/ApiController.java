package ru.gypsyjr.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.gypsyjr.main.Storage;
import ru.gypsyjr.main.models.ApiStatistics;

@RequestMapping
@RestController
public class ApiController {
    @Autowired
    private Storage storage;

    @GetMapping("/statistics")
    public ResponseEntity<ApiStatistics> getStatistics() {
        return ResponseEntity.ok().body(storage.getStatistic());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<String> startIndexing() {
        boolean indexing = storage.indexing();
        JSONObject response = new JSONObject();
        try {
            if (indexing) {
                response.put("result", true);
            } else {
                response.put("result", false);
                response.put("error", "Индексация уже запущена");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<String> stopIndexing() {
        boolean stopIndexing = storage.stopIndexing();
        JSONObject response = new JSONObject();

        try {
            if (!stopIndexing) {
                response.put("result", true);
            } else {
                response.put("result", false);
                response.put("error", "Индексация не запущена");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<String> indexPage(@RequestParam(name = "url") String url) {
        if (url == null) {
            return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
        }

        boolean addPage = storage.indexPage(url);
        JSONObject response = new JSONObject();

        try {
            if (addPage) {
                response.put("result", true);
            } else {
                response.put("result", false);
                response.put("error", "Данная страница находится за пределами сайтов, " +
                        "указанных в конфигурационном файле");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }
}
