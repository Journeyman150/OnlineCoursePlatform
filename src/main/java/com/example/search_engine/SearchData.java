package com.example.search_engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SearchData implements Searcher {
    private final Struct mainStruct;

    public SearchData() {
        mainStruct = new Struct();
    }

    @Override
    public void writeData(long idx, String textField) {
        if(textField == null) {
            return;
        }
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

    @Override
    public void writeData(long idx, String ... textFields) {
        for (int i = 0; i < textFields.length; i++) {
            writeData(idx, textFields[i]);
        }
    }

    @Override
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

    @Override
    public void deleteData(long idx, String ... textFields) {
        for (int i = 0; i < textFields.length; i++) {
            deleteData(idx, textFields[i]);
        }
    }

    @Override
    public Set<Long> findIndexes(String keyword) {
        if (keyword == null || keyword.equals("")) {
                System.out.println("findIndexes if 1");
            return new HashSet<>(Set.of(-1L));
        }

        char[] keyCharSeq = getProcessedLowerCaseCharSeq(keyword);
        if (keyCharSeq.length == 0) {
                System.out.println("findIndexes if 2");
            return new HashSet<>(Set.of(-1L));
        }

        Struct struct = mainStruct;
        if (struct.charMapNode.get(keyCharSeq[0]) == null) {
                System.out.println("findIndexes if 3");
            return new HashSet<>(Set.of(-1L));
        }

        Set<Long> idxSet = new HashSet<>(struct.charMapNode.get(keyCharSeq[0]).charIndexes);
        for (Character ch: keyCharSeq) {
            if (struct.charMapNode.containsKey(ch)) {
                    System.out.println("findIndexes if 4");
                struct = struct.charMapNode.get(ch);
                    System.out.println("Indexes of " + ch + ": " + Arrays.toString(struct.charIndexes.toArray()));
                idxSet.retainAll(struct.charIndexes);
                    System.out.println("Indexes after retain: " + Arrays.toString(idxSet.toArray()));
                    System.out.println();
            } else {
                    System.out.println("findIndexes else 5");
                return new HashSet<>(Set.of(-1L));
            }
        }
        if (idxSet.size() == 0) {
                System.out.println("findIndexes if 6");
            idxSet.add(-1L);
        }
        return idxSet;
    }

    //looks for matching indexes for all words in keyword[]
    @Override
    public Set<Long> findIndexes(String ... keywords) {
        List<Set<Long>> setList = new ArrayList<>();
        for (int i = 0; i < keywords.length; i++) {
            Set<Long> idxSet = findIndexes(keywords[i]);
            if (idxSet.contains(-1L)) {
                return idxSet;
            }
            setList.add(idxSet);
        }
        Set<Long> resIdxSet = setList.get(0);
        for (int i = 0; i < setList.size(); i++) {
            resIdxSet.retainAll(setList.get(i));
        }
        if (resIdxSet.size() == 0) {
            resIdxSet.add(-1L);
        }
        return resIdxSet;
    }

    public static String[] getSeparateKeywords(String str) {
        if (str == null || str.equals("")) {
            return new String[0];
        }
        List<String> keywordsList = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            if (writeAndDeleteRule(str.charAt(i))) {
                stringBuilder.append(Character.toLowerCase(str.charAt(i)));
            } else if (!writeAndDeleteRule(str.charAt(i)) && !stringBuilder.isEmpty()) {
                keywordsList.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        }
        if (!stringBuilder.isEmpty()) {
            keywordsList.add(stringBuilder.toString());
        }
        return keywordsList.toArray(new String[0]);
    }

    public static char[] getProcessedLowerCaseCharSeq(String str) {
        if (str == null || str.equals("")) {
            return new char[0];
        }
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
