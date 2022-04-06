package main.cruncher;

import javafx.util.Pair;
import main.app.App;
import main.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class BagOfWordsTask extends RecursiveTask<Map<ListOfWords<Integer>, Integer>> {
    private final int arity;
    private final int counterLimit;
    private final int start;
    private final int end;
    private final String fileContent;
    private final List<Pair<Integer, Integer>> chunkLocations;

    public BagOfWordsTask(int arity, int counterLimit, int start, int end, String fileContent,
            List<Pair<Integer, Integer>> chunkLocations) {
        this.arity = arity;
        this.counterLimit = counterLimit;
        this.start = start;
        this.end = end;
        this.fileContent = fileContent;
        this.chunkLocations = chunkLocations;
    }

    @Override
    protected Map<ListOfWords<Integer>, Integer> compute() {
        Map<ListOfWords<Integer>, Integer> toReturn = new HashMap<>();
        try {
            if (end - start == 1) {
                Pair<Integer, Integer> pair = chunkLocations.get(start);
                int startIndex = pair.getKey();
                int endIndex = pair.getValue();

//                 String[] words = fileContent.substring(startIndex, endIndex).split(" ");
//                 int operationsThatHappened;
//                 for (int wordCount = 0 ; wordCount < words.length ; ) {
//                 ListOfWords<Integer> listOfWords = new ListOfWords<>();
//                 operationsThatHappened = 0;
//                 for (int i = 0 ; i < arity && wordCount < words.length; i++) {
//                 operationsThatHappened++;
//                 String s = words[wordCount];
//                 listOfWords.addToList(s.hashCode());
//                 wordCount++;
//                 }
//                 wordCount -= (operationsThatHappened - 1);
//                 // Idk just in case
//                 if (listOfWords.getList().size() == arity) {
//                 toReturn.putIfAbsent(listOfWords, 0);
//                 toReturn.put(listOfWords, toReturn.get(listOfWords) + 1);
//                 }
//                 }
//                 // There you go little garbage collector do your job
//                 words = null;


                ListOfWords<Integer> listOfWords = new ListOfWords<>();
                StringBuilder stringBuilder = new StringBuilder("");
                int currentWord = 0;
                char ch;
                int toRemove = 0;

                for (int i = startIndex; i < endIndex; i++) {
                    ch = fileContent.charAt(i);
                    if (currentWord != 0) {
                        toRemove++;
                    }

                    if (ch == ' ') {
                        String str = stringBuilder.toString();
                        stringBuilder.delete(0, str.length());
                        listOfWords.addToList(str.hashCode());
                        currentWord++;

                        if (currentWord == arity) {
                            toReturn.putIfAbsent(listOfWords, 0);
                            toReturn.put(listOfWords, toReturn.get(listOfWords) + 1);

                            listOfWords = new ListOfWords<>();
                            currentWord = 0;

                            i = i - toRemove;
                            toRemove = 0;
                        }
                    } else {
                        stringBuilder.append(ch);
                    }
                }

            } else {
                int mid = ((end - start) / 2) + start;

                BagOfWordsTask left = new BagOfWordsTask(arity, counterLimit, start, mid, fileContent, chunkLocations);
                BagOfWordsTask right = new BagOfWordsTask(arity, counterLimit, mid, end, fileContent, chunkLocations);

                left.fork();
                Map<ListOfWords<Integer>, Integer> rightResult = right.compute();
                Map<ListOfWords<Integer>, Integer> leftResult = left.join();

                toReturn.putAll(rightResult);

                for (var key : leftResult.keySet()) {
                    if (toReturn.containsKey(key)) {
                        toReturn.put(key, toReturn.get(key) + leftResult.get(key));
                    } else {
                        toReturn.put(key, leftResult.get(key));
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            App.finishAppForce();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.gc();
        }
        return toReturn;
    }
}
