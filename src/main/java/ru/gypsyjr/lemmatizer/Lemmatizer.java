package ru.gypsyjr.lemmatizer;

import ru.gypsyjr.db.DBConnection;
import ru.gypsyjr.models.Lemma;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class Lemmatizer {
    private final DBConnection dbConnection;
    private final List<String> WRONG_TYPES = new ArrayList<>();
    private final LuceneMorphology russianMorph;
    private final LuceneMorphology englishMorph;
    private final Map<String, Lemma> wordsCount;
    private final Map<Lemma, Float> wordsRanks;
    private static volatile Lemmatizer instance;

    private Lemmatizer() {
        try {
            russianMorph = new RussianLuceneMorphology();
            englishMorph = new EnglishLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        dbConnection = DBConnection.getInstance();
        wordsCount = new HashMap<>();
        wordsRanks = new HashMap<>();
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

    private void addNewWord(String word, boolean isNew, float rank) {
        if (checkLanguage(word).equals("Russian")) {
            if (!checkRussianForm(word)) {
                return;
            }

            addNormalForms(word, isNew, russianMorph, rank);
        } else if (checkLanguage(word).equals("English")) {
            if (!checkEnglishForm(word)) {
                return;
            }

            addNormalForms(word, isNew, englishMorph, rank);
        }
    }

    private void addNormalForms(String word, boolean isNew, LuceneMorphology wordMorph, float rank) {
        List<String> normalWords = wordMorph.getNormalForms(word);

        normalWords.forEach(it -> {
            Lemma lemma;

            if (wordsCount.containsKey(it) && isNew) {
                lemma = wordsCount.get(it);
                lemma.setFrequency(lemma.getFrequency() + 1);
                wordsCount.replace(it, lemma);
                wordsRanks.clear();

                dbConnection.updateData(lemma);
            } else if (isNew){
                wordsRanks.clear();
                lemma = new Lemma();
                lemma.setFrequency(1);
                lemma.setLemma(it);
                wordsCount.put(it, lemma);
                wordsRanks.put(lemma, rank);

                dbConnection.addClass(lemma);
            } else if (wordsCount.containsKey(it)){
                lemma = wordsCount.get(it);
                if (wordsRanks.containsKey(lemma)) {
                    wordsRanks.replace(lemma, wordsRanks.get(lemma) + rank);
                } else {
                    wordsRanks.put(lemma, rank);
                }
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

    public void addString(String sentence, boolean isNew, float rank) {
        String regex = "[.,!?\\-:;()'\"]?";
        sentence = sentence.replaceAll(regex, "");
        String[] words = sentence.toLowerCase().split(" ");
        Arrays.stream(words).distinct().forEach(it -> {
            addNewWord(it, isNew, rank);
        });
    }

    public void printMorphInfo() {
        wordsCount.forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
    }

    public List<Lemma> getLemmasWithCounts() {
        List<Lemma> lemmas = new ArrayList<>();

        wordsCount.forEach((key, value) -> {
            lemmas.add(value);
        });

        return lemmas;
    }

    public Map<Lemma, Float> getLemmasWithRanks() {
        return wordsRanks;
    }


    //for stage 5



}
