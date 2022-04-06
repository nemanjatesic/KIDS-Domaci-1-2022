package main.cruncher;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import main.logger.Logger;
import main.output.BOWFutureAndFileName;
import main.output.CacheOutput;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

public class CheckIfTaskDone implements Runnable {
    private final BOWFutureAndFileName bowFutureAndFileName;
    private final ObservableList<String> resultListOutputs;
    private final ObservableList<String> crunchingFilePaths;
    private final String filePathWithoutAritySuffix;
    private final BlockingQueue<BOWFutureAndFileName> inputQueForOutput;

    public CheckIfTaskDone(BOWFutureAndFileName bowFutureAndFileName, ObservableList<String> resultListOutputs, ObservableList<String> crunchingFilePaths, String filePathWithoutAritySuffix, BlockingQueue<BOWFutureAndFileName> inputQueForOutput) {
        this.bowFutureAndFileName = bowFutureAndFileName;
        this.resultListOutputs = resultListOutputs;
        this.crunchingFilePaths = crunchingFilePaths;
        this.filePathWithoutAritySuffix = filePathWithoutAritySuffix;
        this.inputQueForOutput = inputQueForOutput;
    }

    @Override
    public void run() {
        try {
            // Wait until future is done and then update observable list to stop showing "*" and remove from crunching list
            bowFutureAndFileName.getBowFuture().get();

            Platform.runLater(() -> {
                String filePath = bowFutureAndFileName.getFilePath();
                int index = resultListOutputs.indexOf("*" + filePath);
                if (index == -1) {
                    index = resultListOutputs.indexOf(filePath);
                    try {
                        inputQueForOutput.put(bowFutureAndFileName);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (index == -1) {
                        return;
                    }
                }
                resultListOutputs.set(index, filePath);
                crunchingFilePaths.remove(filePathWithoutAritySuffix);
            });
        } catch (Exception e) {
            if (Logger.debugEnabled) {
                e.printStackTrace();
            }
        }
    }
}
