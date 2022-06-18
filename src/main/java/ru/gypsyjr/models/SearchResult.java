package ru.gypsyjr.models;

public class SearchResult implements Comparable<SearchResult>{
    private String uri;
    private String title;
    private String snippet;
    private float relevance;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public float getRelevance() {
        return relevance;
    }

    public void setRelevance(float relevance) {
        this.relevance = relevance;
    }

    @Override
    public int compareTo(SearchResult o) {
        if (relevance > o.getRelevance()){
            return -1;
        }
        return 1;
    }

    @Override
    public String toString() {
        return "title: " + title + "\nuri: " + uri + "\nsnippet:\n" + snippet + "\nrel: " + relevance;
    }
}
