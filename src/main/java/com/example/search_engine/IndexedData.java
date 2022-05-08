package com.example.search_engine;

public class IndexedData {
    private long idx;
    private String[] dataArr;

    public IndexedData(long idx, String ... dataArr) {
        this.idx = idx;
        this.dataArr = dataArr;
    }

    public long getIdx() {
        return idx;
    }

    public String[] getDataArr() {
        return dataArr;
    }
}
