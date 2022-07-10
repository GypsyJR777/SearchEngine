package ru.gypsyjr.main.models;

import java.util.Set;

public class Search {
    private boolean result;
    private int count;
    private Set<SearchResult> data;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Set<SearchResult> getData() {
        return data;
    }

    public void setData(Set<SearchResult> data) {
        this.data = data;
        setCount(data.size());
    }
}
