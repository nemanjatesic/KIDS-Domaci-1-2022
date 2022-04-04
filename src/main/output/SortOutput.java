package main.output;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import main.cruncher.ListOfWords;
import main.view.MainView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SortOutput implements Runnable {
    private final Map<ListOfWords<Integer>, Integer> resultDots;
    private final int sortProgressLimit;

    public SortOutput(Map<ListOfWords<Integer>, Integer> resultDots, int sortProgressLimit) {
        this.resultDots = resultDots;
        this.sortProgressLimit = sortProgressLimit;
    }

    @Override
    public void run() {
        var wrapper = new Object(){ int sortCurrentProgressCounter = 0; };
        Label label = new Label("Sorting");
        ProgressBar progressBar = new ProgressBar();
        int resultSize = resultDots.keySet().size();
        int maxNumberOfCompares = (int) (resultSize * Math.log(resultSize));

        Platform.runLater(() -> {
            MainView.right.getChildren().add(label);
            MainView.right.getChildren().add(progressBar);
        });

        List<ListOfWords<Integer>> list = resultDots.keySet().stream().sorted((a, b) -> {
            if (wrapper.sortCurrentProgressCounter++ % sortProgressLimit == 0) {
                float currentProgress = wrapper.sortCurrentProgressCounter / (maxNumberOfCompares * 1f);
                Platform.runLater(() -> progressBar.setProgress(currentProgress));
            }

            return resultDots.get(b) - resultDots.get(a);
        }).limit(100).collect(Collectors.toList());

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        for (int i = 0 ; i < list.size() ; i++) {
            series.getData().add(new XYChart.Data<>(i, resultDots.get(list.get(i))));
        }

        Platform.runLater(() -> {
            MainView.lineChart.getData().clear();
            MainView.lineChart.getData().addAll(series);
            MainView.right.getChildren().remove(label);
            MainView.right.getChildren().remove(progressBar);
        });
    }
}
