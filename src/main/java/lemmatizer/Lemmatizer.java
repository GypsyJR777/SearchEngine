package lemmatizer;

import db.DBConnection;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

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

    private void addNewWord(String word) {
        if (checkLanguage(word).equals("Russian")) {
            if (!checkRussianForm(word)) {
                return;
            }

            wordsBaseForms.put(word, russianMorph.getNormalForms(word));

            if (wordsCount.containsKey(word)) {
                wordsCount.replace(word, wordsCount.get(word) + 1);
            } else {
                wordsCount.put(word, 1);
            }
        } else if (checkLanguage(word).equals("English")) {
            if (!checkEnglishForm(word)) {
                return;
            }

            wordsBaseForms.put(word, englishMorph.getNormalForms(word));

            if (wordsCount.containsKey(word)) {
                wordsCount.replace(word, wordsCount.get(word) + 1);
            } else {
                wordsCount.put(word, 1);
            }
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
        Arrays.stream(words).distinct().forEach(this::addNewWord);
    }

    public void printMorphInfo() {
        wordsCount.forEach((key, value) -> {
            System.out.println(key + ": " + value);

//            if (checkLanguage(key).equals("Russian")) {
//                System.out.println(russianMorph.getMorphInfo(key));
//            } else if (checkLanguage(key).equals("English")) {
//                System.out.println(englishMorph.getMorphInfo(key));
//            }
        });
    }
}
