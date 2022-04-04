package main.output;

import javafx.collections.ObservableList;
import main.app.App;
import main.cruncher.CounterCruncher;
import main.cruncher.ListOfWords;
import main.logger.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CacheOutput implements Runnable {
    private final List<CounterCruncher> crunchers = new CopyOnWriteArrayList<>();
    private final BlockingQueue<BOWFutureAndFileName> inputQue = new LinkedBlockingQueue<>();
    private final ObservableList<String> resultList;
    private final Map<String, Future<Map<ListOfWords<Integer>, Integer>>> filePathToResult = new ConcurrentHashMap<>();
    private final int sortProgressLimit;

    public CacheOutput(ObservableList<String> resultList, int sortProgressLimit) {
        this.resultList = resultList;
        this.sortProgressLimit = sortProgressLimit;
    }

    @Override
    public void run() {
        Logger.info("Started running CacheOutput, thread name: " + Thread.currentThread().getName() + ", thread id: "
                + Thread.currentThread().getId());
        while (true) {
            try {
                BOWFutureAndFileName futureAndFileName = inputQue.take();

                if (futureAndFileName.getFilePath().equals(App.POISON_PILL_NAME)) {
                    break;
                }

                filePathToResult.put(futureAndFileName.getFilePath(), futureAndFileName.getBowFuture());

                Logger.info(String.format(
                        "CacheOutput added futureAndFileName object with path: [%s] into filePathToResult map",
                        futureAndFileName.getFilePath()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Logger.info("Finished running CacheOutput, thread name: " + Thread.currentThread().getName() + ", thread id: "
                + Thread.currentThread().getId());
    }

    public Map<ListOfWords<Integer>, Integer> poll(String resultName) {
        var result = filePathToResult.get(resultName);
        if (result.isDone()) {
            try {
                return result.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Map<ListOfWords<Integer>, Integer> take(String str) {
        try {
            return filePathToResult.get(str).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sum(String sumName, Summation summation) {
        filePathToResult.put(sumName, App.outputThreadPool.submit(summation));
    }

    public void stop() {
        try {
            inputQue.put(new BOWFutureAndFileName(App.POISON_PILL_NAME, null));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<CounterCruncher> getCrunchers() {
        return crunchers;
    }

    public BlockingQueue<BOWFutureAndFileName> getInputQue() {
        return inputQue;
    }

    public ObservableList<String> getResultList() {
        return resultList;
    }

    public Map<String, Future<Map<ListOfWords<Integer>, Integer>>> getFilePathToResult() {
        return filePathToResult;
    }

    public int getSortProgressLimit() {
        return sortProgressLimit;
    }
}
