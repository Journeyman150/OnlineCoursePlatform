package com.example.search_engine;

import java.util.Set;

public interface Searcher {

    void writeData(long idx, String textField);

    void writeData(long idx, String ... textFields);

    void deleteData(long idx, String textField);

    void deleteData(long idx, String ... textFields);

    Set<Long> findIndexes(String keyword);

    Set<Long> findIndexes(String ... keywords);

}
