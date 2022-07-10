package ru.gypsyjr.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gypsyjr.main.Storage;

@RequestMapping
@RestController
public class SearchController {
    @Autowired
    private Storage storage;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(name = "query") String query,
                                    @RequestParam(name = "site", required = false) String site,
                                    @RequestParam(name = "offset", defaultValue = "0") int offset,
                                    @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return new ResponseEntity<>(storage.search(query, site, offset, limit), HttpStatus.OK);
    }
}
