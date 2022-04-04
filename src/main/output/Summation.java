package main.output;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import main.cruncher.ListOfWords;
import main.view.MainView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Summation implements Callable<Map<ListOfWords<Integer>, Integer>> {
    private final List<String> selectedResults;
    private final CacheOutput cacheOutput;
    private final String selectedName;
    private final ObservableList<String> resultList;

    public Summation(List<String> selectedResults, CacheOutput cacheOutput, String selectedName, ObservableList<String> resultList) {
        this.selectedResults = selectedResults;
        this.cacheOutput = cacheOutput;
        this.selectedName = selectedName;
        this.resultList = resultList;
    }

    @Override
    public Map<ListOfWords<Integer>, Integer> call() throws Exception {
        try {
            ProgressBar progressBar = new ProgressBar();
            Label label = new Label("Summation in progress");
            Platform.runLater(() -> {
                resultList.add("*" + selectedName);
                MainView.right.getChildren().add(label);
                MainView.right.getChildren().add(progressBar);
            });

            var wrapper = new Object(){ int summationCurrentProgressCounter = 0; };
            Map<ListOfWords<Integer>, Integer> toReturn = new HashMap<>();
            List<Map<ListOfWords<Integer>, Integer>> listOfSelected = selectedResults.stream().map(res -> cacheOutput.take(res.replace("*", ""))).collect(Collectors.toList());

            int listSize = listOfSelected.size();

            for (var map : listOfSelected) {
                wrapper.summationCurrentProgressCounter++;
                for (var key : map.keySet()) {
                    if (toReturn.containsKey(key)) {
                        toReturn.put(key, toReturn.get(key) + map.get(key));
                    } else {
                        toReturn.put(key, map.get(key));
                    }
                }

                Platform.runLater(() -> {
                    float currentProgress = (wrapper.summationCurrentProgressCounter / (listSize * 1f)) * 100;
                    progressBar.setProgress(currentProgress);
                });
            }

            Platform.runLater(() -> {
                MainView.right.getChildren().remove(label);
                MainView.right.getChildren().remove(progressBar);
                resultList.set(resultList.indexOf("*" + selectedName), selectedName);
            });

            return toReturn;
        } catch (Exception e) {
            return null;
        }
    }
}
