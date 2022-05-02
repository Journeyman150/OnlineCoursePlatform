package com.example.search_engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SearchData {
    private final Struct mainStruct;

    public SearchData() {
        mainStruct = new Struct();
    }

    public void writeData(long idx, String textField) {
        char[] textCharSeq = textField.toCharArray();
        Struct struct = mainStruct;
        for (int i = 0; i < textCharSeq.length; i++) {
            char ch = Character.toLowerCase(textCharSeq[i]);
            if (writeAndDeleteRule(ch)) {
                if (struct.charMapNode.containsKey(ch)) {
                    struct = struct.charMapNode.get(ch);
                    struct.charIndexes.add(idx);
                } else {
                    Struct newStruct = new Struct();
                    struct.charMapNode.put(ch, newStruct);
                    struct = newStruct;
                    struct.charIndexes.add(idx);
                }
            } else {
                struct = mainStruct;
            }
        }
    }

    public void writeData(long idx, String ... textFields) {
        for (int i = 0; i < textFields.length; i++) {
            writeData(idx, textFields[i]);
        }
    }

    public void deleteData(long idx, String textField) {
        Struct struct = mainStruct;
        char[] textCharSeq = textField.toCharArray();
        for (int i = 0; i < textCharSeq.length; i++) {
            char ch = Character.toLowerCase(textCharSeq[i]);
            if (writeAndDeleteRule(ch)) {
                if (struct.charMapNode.containsKey(ch)) {
                    struct = struct.charMapNode.get(ch);
                    struct.charIndexes.remove(idx);
                }
            } else {
                struct = mainStruct;
            }
        }
    }

    public void deleteData(long idx, String ... textFields) {
        for (int i = 0; i < textFields.length; i++) {
            deleteData(idx, textFields[i]);
        }
    }

    public Set<Long> findIndexes(String keyword) {
        Struct struct = mainStruct;
        char[] keyCharSeq = getProcessedLowerCaseCharSeq(keyword);

        if (struct.charMapNode.get(keyCharSeq[0]) == null) {
            return new HashSet<>(Set.of(-1L));
        }
        Set<Long> idxSet = struct.charMapNode.get(keyCharSeq[0]).charIndexes;

        for (Character ch: keyCharSeq) {
            if (struct.charMapNode.containsKey(ch)) {
                struct = struct.charMapNode.get(ch);
                idxSet.retainAll(struct.charIndexes);
            } else return new HashSet<>(Set.of(-1L));
        }
        if (idxSet.size() == 0) {
            idxSet.add(-1L);
        }
        return idxSet;
    }

    public Set<Long> findMatchingIndexes(String ... keywords) {
        List<Set<Long>> setList = new ArrayList<>();
        for (int i = 0; i < keywords.length; i++) {
            Set<Long> idxSet = findIndexes(keywords[i]);
            if (idxSet.contains(-1L)) {
                return idxSet;
            }
            setList.add(idxSet);
        }
        Set<Long> resIdxSet = setList.get(0);
        for (int i = 1; i < setList.size(); i++) {
            resIdxSet.retainAll(setList.get(i));
        }
        if (resIdxSet.size() == 0) {
            resIdxSet.add(-1L);
        }
        return resIdxSet;
    }

    public static char[] getProcessedLowerCaseCharSeq(String str) {
        char[] preparedCharSeq = new char[str.length()];
        int j = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (writeAndDeleteRule(ch)) {
                preparedCharSeq[j++] = Character.toLowerCase(ch);
            }
        }
        char[] processedCharSeq = new char[j];
        for (int i = 0; i < processedCharSeq.length; i++) {
            processedCharSeq[i] = preparedCharSeq[i];
        }
        return processedCharSeq;
    }

    private static boolean writeAndDeleteRule(char ch) {
        return (Character.isLetter(ch) || Character.isDigit(ch));
    }

    private class Struct {
        Set<Long> charIndexes;
        ConcurrentHashMap<Character, Struct> charMapNode;

        Struct() {
            charIndexes = ConcurrentHashMap.newKeySet();
            charMapNode = new ConcurrentHashMap<>();
        }
    }
}
