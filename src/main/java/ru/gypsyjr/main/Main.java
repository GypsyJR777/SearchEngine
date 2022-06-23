package ru.gypsyjr.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.gypsyjr.db.DBConnection;
import ru.gypsyjr.lemmatizer.Lemmatizer;
import ru.gypsyjr.models.Lemma;
import ru.gypsyjr.parse.WebMapParse;
import ru.gypsyjr.search.SearchEngine;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

@SpringBootApplication
@EnableJpaRepositories
@EnableConfigurationProperties()
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}
