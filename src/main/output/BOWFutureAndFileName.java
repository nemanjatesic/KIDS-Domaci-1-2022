package main.output;

import main.cruncher.ListOfWords;

import java.util.Map;
import java.util.concurrent.Future;

public class BOWFutureAndFileName {
    private String filePath;
    private Future<Map<ListOfWords<Integer>, Integer>> bowFuture;

    public BOWFutureAndFileName(String filePath, Future<Map<ListOfWords<Integer>, Integer>> bowFuture) {
        this.filePath = filePath;
        this.bowFuture = bowFuture;
    }

    public String getFilePath() {
        return filePath;
    }

    public Future<Map<ListOfWords<Integer>, Integer>> getBowFuture() {
        return bowFuture;
    }
}
