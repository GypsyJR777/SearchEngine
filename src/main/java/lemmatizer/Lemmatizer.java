package lemmatizer;

import db.DBConnection;
import models.Field;
import models.Lemma;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import parse.WebMapParse;

import java.io.IOException;
import java.util.*;

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
        WRONG_TYPES.add("МС");
        WRONG_TYPES.add("CONJ");
        WRONG_TYPES.add("PART");
    }

    private boolean checkRussianForm(String word) {
        String russianAlphabet = "[а-яА-Я]+";

        if (!word.matches(russianAlphabet)) {
            return false;
        }

        List<String> wordBaseForm = russianMorph.getMorphInfo(word);

        for (String type : WRONG_TYPES) {
            if (wordBaseForm.toString().contains(type)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkEnglishForm(String word) {

        List<String> wordBaseForm = englishMorph.getMorphInfo(word);

        for (String type : WRONG_TYPES) {
            if (wordBaseForm.toString().contains(type)) {
                return false;
            }
        }

        return true;
    }

    private String checkLanguage(String word) {
        String russianAlphabet = "[а-яА-Я]+";
        String englishAlphabet = "[a-zA-z]+";

        if (word.matches(russianAlphabet)) {
            return "Russian";
        } else if (word.matches(englishAlphabet)) {
            return "English";
        } else {
            return "";
        }
    }

    private void addNewWord(String word, float frequency) {
        if (checkLanguage(word).equals("Russian")) {
            if (!checkRussianForm(word)) {
                return;
            }

            addNormalForms(word, frequency, russianMorph);
        } else if (checkLanguage(word).equals("English")) {
            if (!checkEnglishForm(word)) {
                return;
            }

            addNormalForms(word, frequency, englishMorph);
        }
    }

    private void addNormalForms(String word, float frequency, LuceneMorphology wordMorph) {
        wordsBaseForms.put(word, wordMorph.getNormalForms(word));
        List<String> normalWords = wordMorph.getNormalForms(word);

        normalWords.forEach(it -> {
            if (wordsCount.containsKey(it)) {
                wordsCount.replace(it, wordsCount.get(it) + 1);
            } else {
                wordsCount.put(it, 1);
            }
        });
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

    public void addString(String sentence, float frequency) {
        String regex = "[.,!?\\-:;()'\"]?";
        sentence = sentence.replaceAll(regex, "");
        String[] words = sentence.toLowerCase().split(" ");
        Arrays.stream(words).distinct().forEach(it -> {
            addNewWord(it, frequency);
        });
    }

    public void printMorphInfo() {
//        wordsBaseForms.forEach((key, value) -> {
//            System.out.println(key + ": " + value.toString());
//        });
        wordsCount.forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
    }

    public List<Lemma> getLemmas() {
        List<Lemma> lemmas = new ArrayList<>();

        wordsCount.forEach((key, value) -> {
            Lemma lemma = new Lemma();
            lemma.setLemma(key);
            lemma.setFrequency(value);
            lemmas.add(lemma);
        });

        return lemmas;
    }
}
