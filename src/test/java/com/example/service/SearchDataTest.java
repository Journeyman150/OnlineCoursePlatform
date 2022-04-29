package com.example.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SearchDataTest {
    private SearchData searchData;

    @Before
    public void setup() {
        searchData = new SearchData();
        searchData.writeData(1, "1word", "слово1 и нек.оторый текст,", "индекс");
        searchData.writeData(2, "СЛОВО2");
        searchData.writeData(3, "кор-откий* текст,", "some words in text. 333");
        searchData.writeData(4, "cyberpunk4");
        searchData.writeData(5, "Одинаковый* текст с индексами 5 и 6");
        searchData.writeData(6, "оДиНакОвЫй* текст с индексами 5 и 6");
        searchData.writeData(7, "7");
        searchData.writeData(8, "8", "88");
        searchData.deleteData(8, "8", "88");
    }

    @Test
    public void testFindIndexes() {
        assertArrayEquals(new Long[]{1L}, searchData.findIndexes("1woRD").toArray(new Long[0]));
        assertArrayEquals(new Long[]{1L, 5L, 6L}, searchData.findIndexes(" иНдекс ").toArray(new Long[0]));
        assertArrayEquals(new Long[]{3L}, searchData.findIndexes(" 3/3").toArray(new Long[0]));
        assertArrayEquals(new Long[]{-1L}, searchData.findIndexes("987s5ds7").toArray(new Long[0]));
        assertArrayEquals(new Long[]{-1L}, searchData.findIndexes("8").toArray(new Long[0]));
    }

    @Test
    public void testFindMatchingIndexes() {
        assertArrayEquals(new Long[]{5L, 6L}, searchData.findMatchingIndexes("одинаковый", "5", "6", "индекс").toArray(new Long[0]));
        assertArrayEquals(new Long[]{-1L}, searchData.findMatchingIndexes("одинаковый", "7", "6", "индекс").toArray(new Long[0]));
        assertArrayEquals(new Long[]{-1L}, searchData.findMatchingIndexes("одинаковый", "eqwfjnewkfl", "6", "индекс").toArray(new Long[0]));
        assertArrayEquals(new Long[]{-1L}, searchData.findMatchingIndexes("8", "88").toArray(new Long[0]));
    }

    @Test
    public void testGetProcessedLowerCaseCharSeq() {
        assertArrayEquals(new char[0], SearchData.getProcessedLowerCaseCharSeq(""));
        assertArrayEquals(new char[0], SearchData.getProcessedLowerCaseCharSeq(" ,"));
        assertArrayEquals(new char[]{'w','o','r','d'}, SearchData.getProcessedLowerCaseCharSeq("word"));
        assertArrayEquals(new char[]{'w','o','r','d'}, SearchData.getProcessedLowerCaseCharSeq(", ,word. "));
        assertArrayEquals(new char[]{'w','o','r','d'}, SearchData.getProcessedLowerCaseCharSeq("w/ oRD"));
        assertArrayEquals(new char[]{'1','w','o','r','d','9'}, SearchData.getProcessedLowerCaseCharSeq("1Word9"));
        assertArrayEquals(new char[]{'3','2','9','0'}, SearchData.getProcessedLowerCaseCharSeq("3290"));
    }

}