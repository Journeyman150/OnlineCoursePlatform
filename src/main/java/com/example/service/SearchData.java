package com.example.service;

import java.util.*;

public class SearchData {
    private Struct struct;
    private final Struct mainStruct;

    public SearchData() {
        mainStruct = new Struct();
        struct = mainStruct;
    }

    public void writeData(long idx, String textField) {
        char[] textCharSeq = textField.toCharArray();
        for (int i = 0; i < textCharSeq.length; i++) {
            char ch = textCharSeq[i];
            if (Character.isLetter(ch) || Character.isDigit(ch)) {
                this.addElementAndMovePointer(idx, Character.toLowerCase(ch));
            } else {
                struct = mainStruct;
            }
        }
        struct = mainStruct;
    }

    public void writeData(long idx, String ... textFields) {
        for (int i = 0; i < textFields.length; i++) {
            writeData(idx, textFields[i]);
        }
    }

    private void addElementAndMovePointer(Long idx, Character ch) {
        if (struct.charMapNode.containsKey(ch)) {
            struct = struct.charMapNode.get(ch);
            struct.charIndexes.add(idx);
        } else {
            Struct newStruct = new Struct();
            struct.charMapNode.put(ch, newStruct);
            struct = newStruct;
            struct.charIndexes.add(idx);
        }
    }

    public Set<Long> findIndexes(String keyword) {
        struct = mainStruct;
        char[] keyCharSeq = getProcessedLowerCaseCharSeq(keyword);

        if (struct.charMapNode.get(keyCharSeq[0]) == null) {
            return new HashSet<>(Set.of(-1L));
        }
        HashSet<Long> idxSet = struct.charMapNode.get(keyCharSeq[0]).charIndexes;

        for (Character ch: keyCharSeq) {
            if (struct.charMapNode.containsKey(ch)) {
                struct = struct.charMapNode.get(ch);
                idxSet.retainAll(struct.charIndexes);
            } else {
                return new HashSet<>(Set.of(-1L));
            }
        }
        return idxSet;
    }

    public Set<Long> findMatchingIndexes(String ... keywords) {
        List<Set<Long>> setList = new ArrayList<>();
        for (int i = 0; i < keywords.length; i++) {
            Set<Long> idxSet = findIndexes(keywords[i]);
            setList.add(idxSet);
        }
        for (int i = 1; i < setList.size(); i++) {
            setList.get(0).retainAll(setList.get(i));
        }
        return setList.get(0);
    }

    public static char[] getProcessedLowerCaseCharSeq(String str) {
        char[] preparedCharSeq = new char[str.length()];
        int j = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isLetter(ch) || Character.isDigit(ch)) {
                preparedCharSeq[j++] = Character.toLowerCase(ch);
            }
        }
        char[] processedCharSeq = new char[j];
        for (int i = 0; i < processedCharSeq.length; i++) {
            processedCharSeq[i] = preparedCharSeq[i];
        }
        return processedCharSeq;
    }

    private class Struct {
        HashSet<Long> charIndexes;
        HashMap<Character, Struct> charMapNode;

        Struct() {
            charIndexes = new HashSet<>();
            charMapNode = new HashMap<>();
        }
    }
}
