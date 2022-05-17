package lemmatizer;

import db.DBConnection;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Lemmatizer {
    private final List<String> WRONG_TYPES = new ArrayList<>();
    private final LuceneMorphology russianMorph;
    private final LuceneMorphology englishMorph;
    private final Map<String, List<String>> wordsBaseForms;
    private final Map<String, Integer> wordsCount;
    private static volatile Lemmatizer instance;

    private Lemmatizer() {
        try {
            russianMorph = new RussianLuceneMorphology();
            englishMorph = new EnglishLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        wordsBaseForms = new HashMap<>();
        wordsCount = new HashMap<>();
        WRONG_TYPES.add("ПРЕДЛ");
        WRONG_TYPES.add("СОЮЗ");
        WRONG_TYPES.add("МЕЖД");
        WRONG_TYPES.add("ВВОДН");
        WRONG_TYPES.add("ЧАСТ");
    }

    private boolean checkForm(String word) {
        List<String> wordBaseForm = russianMorph.getMorphInfo(word);

        for (String type : WRONG_TYPES) {
            if (wordBaseForm.toString().contains(type)) {
                return false;
            }
        }

        return true;
    }

    private void addNewWord(String word) {
        wordsBaseForms.put(word, russianMorph.getNormalForms(word));

        if (wordsCount.containsKey(word)) {
            wordsCount.replace(word, wordsCount.get(word) + 1);
        } else {
            wordsCount.put(word, 1);
        }
    }

    public static Lemmatizer getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new Lemmatizer();
                }
            }
        }

        return instance;
    }

    public void addString(String sentence) {
        String regex = "[.,!?\\-:;()'\"]?";
        sentence = sentence.replaceAll(regex, "");
        String[] words = sentence.toLowerCase().split(" ");
        Arrays.stream(words).distinct().forEach(it -> {
            if (checkForm(it)) {
                addNewWord(it);
            }
        });
    }

    public void printMorphInfo() {
        wordsCount.forEach((key, value) -> {
            System.out.println(key + ": " + value);
            System.out.println(russianMorph.getMorphInfo(key));
        });
    }
}
