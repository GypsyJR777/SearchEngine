package ru.gypsyjr.main.models;

import java.util.SortedSet;

public class Search {
    private boolean result;
    private int count;
    private SortedSet<SearchResult> data;

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

    public SortedSet<SearchResult> getData() {
        return data;
    }

    public void setData(SortedSet<SearchResult> data) {
        this.data = data;
        setCount(data.size());
    }
}
