package ru.gypsyjr.search;

import ru.gypsyjr.lemmatizer.Lemmatizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchEngine {
    private List<String> words;
    private Lemmatizer lemmatizer;

    public SearchEngine() {
        words = new ArrayList<>();
        lemmatizer = Lemmatizer.getInstance();
    }

    public void addSearchQuery(String query) {
        //TODO заменить цикл на 2, сначала находим леммы потом достаем индексы (отдельно нужны 2 функции в Lemmatizer)

        Map<String, List<?>> words = new HashMap<>();

        for (String word: query.split(" ")) {
            List<?> indexes = lemmatizer.searchLemmas(word.toLowerCase());
            if (indexes.size() > 0) {
                words.put(word, indexes);
            }
        }
    }
}
