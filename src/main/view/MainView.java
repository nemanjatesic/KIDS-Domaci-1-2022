package main.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.app.App;
import main.app.Config;
import main.cruncher.ListOfWords;
import main.input.FileInput;
import main.cruncher.CounterCruncher;
import main.logger.Logger;
import main.model.Disk;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main.output.CacheOutput;
import main.output.SortOutput;
import main.output.Summation;

public class MainView {
	private Stage stage;
	private ComboBox<Disk> disks;
	private HBox left;
	private VBox fileInput, cruncher;
	public static Pane center, right;
	private ListView<String> results;
	private Button addFileInput, singleResult, sumResult;
	public static ArrayList<FileInputView> fileInputViews;
	public static LineChart<Number, Number> lineChart;
	private ArrayList<CounterCruncher> availableCrunchers;

	private ObservableList<String> outputResultList;
	public static CacheOutput cacheOutput;

	public void initMainView(BorderPane borderPane, Stage stage) {
		outputResultList = FXCollections.observableArrayList();
		cacheOutput = new CacheOutput(outputResultList, Integer.parseInt(Config.getProperty("sort_progress_limit")));

		this.stage = stage;

		fileInputViews = new ArrayList<>();
		availableCrunchers = new ArrayList<>();

		left = new HBox();

		borderPane.setLeft(left);

		initFileInput();

		initCruncher();

		initCenter(borderPane);

		initRight(borderPane);

		App.outputThreadPool.submit(cacheOutput);
	}

	private void initFileInput() {
		fileInput = new VBox();

		fileInput.getChildren().add(new Text("File inputs:"));
		VBox.setMargin(fileInput.getChildren().get(0), new Insets(0, 0, 10, 0));

		disks = new ComboBox<Disk>();
		disks.getSelectionModel().selectedItemProperty().addListener(e -> updateEnableAddFileInput());
		disks.setMinWidth(120);
		disks.setMaxWidth(120);
		fileInput.getChildren().add(disks);

		addFileInput = new Button("Add FileInput");
		int sleeptime = Integer.parseInt(Config.getProperty("file_input_sleep_time"));
		addFileInput
				.setOnAction(e -> addFileInput(new FileInput(sleeptime, disks.getSelectionModel().getSelectedItem())));
		VBox.setMargin(addFileInput, new Insets(5, 0, 10, 0));
		addFileInput.setMinWidth(120);
		addFileInput.setMaxWidth(120);
		fileInput.getChildren().add(addFileInput);

		int width = 210;

		VBox divider = new VBox();
		divider.getStyleClass().add("divider");
		divider.setMinWidth(width);
		divider.setMaxWidth(width);
		fileInput.getChildren().add(divider);
		VBox.setMargin(divider, new Insets(0, 0, 15, 0));

		Insets insets = new Insets(10);
		ScrollPane scrollPane = new ScrollPane(fileInput);
		scrollPane.setMinWidth(width + 35);
		fileInput.setPadding(insets);
		fileInput.getChildren().add(scrollPane);

		left.getChildren().add(scrollPane);

		try {
			String[] disksArray = Config.getProperty("disks").split(";");
			for (String disk : disksArray) {
				File file = new File(disk);
				System.out.println(file.getAbsolutePath());
				if (!file.exists() || !file.isDirectory()) {
					System.out.println("here");
					throw new Exception("Bad directory path");
				}
				disks.getItems().add(new Disk(file));
			}
			if (disksArray.length > 0) {
				disks.getSelectionModel().select(0);
			}
		} catch (Exception e) {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Closing");
				alert.setHeaderText("Bad config disks");
				alert.setContentText(null);

				alert.showAndWait();
				System.exit(0);
			});
		}

		updateEnableAddFileInput();
	}

	private void initCruncher() {
		cruncher = new VBox();

		Text text = new Text("Crunchers");
		cruncher.getChildren().add(text);
		VBox.setMargin(text, new Insets(0, 0, 5, 0));

		Button addCruncher = new Button("Add cruncher");
		addCruncher.setOnAction(e -> addCruncher());
		cruncher.getChildren().add(addCruncher);
		VBox.setMargin(addCruncher, new Insets(0, 0, 15, 0));

		int width = 110;

		Insets insets = new Insets(10);
		ScrollPane scrollPane = new ScrollPane(cruncher);
		scrollPane.setMinWidth(width + 35);
		cruncher.setPadding(insets);
		left.getChildren().add(scrollPane);
	}

	private void initCenter(BorderPane borderPane) {
		center = new HBox();

		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("Bag of words");
		yAxis.setLabel("Frequency");
		lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		lineChart.setMinWidth(700);
		lineChart.setMinHeight(600);
		center.getChildren().add(lineChart);

		borderPane.setCenter(center);
	}

	private void initRight(BorderPane borderPane) {
		right = new VBox();
		right.setPadding(new Insets(10));
		right.setMaxWidth(200);

		results = new ListView<String>(this.outputResultList);
		right.getChildren().add(results);
		VBox.setMargin(results, new Insets(0, 0, 10, 0));
		results.getSelectionModel().selectedItemProperty().addListener(e -> updateResultButtons());
		results.getSelectionModel().selectedIndexProperty().addListener(e -> updateResultButtons());
		results.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		singleResult = new Button("Single result");
		singleResult.setOnAction(e -> getSingleResult());
		singleResult.setDisable(true);
		right.getChildren().add(singleResult);
		VBox.setMargin(singleResult, new Insets(0, 0, 5, 0));

		sumResult = new Button("Sum results");
		sumResult.setDisable(true);
		sumResult.setOnAction(e -> sumResults());
		right.getChildren().add(sumResult);
		VBox.setMargin(sumResult, new Insets(0, 0, 10, 0));

		borderPane.setRight(right);
	}

	public void updateEnableAddFileInput() {
		Disk disk = disks.getSelectionModel().getSelectedItem();
		if (disk != null) {
			for (FileInputView fileInputView : fileInputViews) {
				if (fileInputView.getFileInput().getDisk() == disk) {
					addFileInput.setDisable(true);
					return;
				}
			}
			addFileInput.setDisable(false);
		} else {
			addFileInput.setDisable(true);
		}
	}

	public void updateResultButtons() {
		if (results.getSelectionModel().getSelectedItems() == null
				|| results.getSelectionModel().getSelectedItems().size() == 0) {
			singleResult.setDisable(true);
			sumResult.setDisable(true);
		} else if (results.getSelectionModel().getSelectedItems().size() == 1) {
			singleResult.setDisable(false);
			sumResult.setDisable(true);
		} else {
			singleResult.setDisable(true);
			sumResult.setDisable(false);
		}
	}

	private void getSingleResult() {
		// This list must always be of 1 element but do check just in case
		List<String> selectedItems = this.results.getSelectionModel().getSelectedItems();

		if (selectedItems.size() != 1) {
			Logger.warning("Selected items are either 0 or selected items are longer then 1, actual size: "
					+ selectedItems.size());
			return;
		}

		String selected = selectedItems.get(0);
		// Just in case
		if (selected.startsWith("*")) {
			this.resultNotReadyWarning();
			return;
		}

		Map<ListOfWords<Integer>, Integer> result = this.cacheOutput.poll(selected);

		if (result == null) {
			this.resultNotReadyWarning();
			return;
		}

		App.outputThreadPool
				.submit(new SortOutput(result, Integer.parseInt(Config.getProperty("sort_progress_limit"))));
		Logger.info("Future is done!");
	}

	private void resultNotReadyWarning() {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Error");
		alert.setHeaderText("Result not ready yet!");
		alert.setContentText(null);
		alert.showAndWait();
	}

	private void unknownErrorOccurredWarning() {
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Error");
		alert.setHeaderText("An unknown error occurred");
		alert.setContentText(null);
		alert.showAndWait();
	}

	private void sumResults() {
		TextInputDialog dialog = new TextInputDialog("sum");
		dialog.setTitle("Confirmation");
		dialog.setHeaderText("Enter unique sum name");

		Optional<String> optionalResult = dialog.showAndWait();
		if (optionalResult.isEmpty()) {
			return;
		}

		String name = optionalResult.get();
		if (this.cacheOutput.getResultList().contains(name) || this.cacheOutput.getResultList().contains("*" + name)) {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Error");
			alert.setHeaderText("That name already exists");
			alert.setContentText(null);
			alert.showAndWait();
			return;
		}

		List<String> selectedItems = this.results.getSelectionModel().getSelectedItems();

		Summation summation = new Summation(selectedItems, this.cacheOutput, name, this.cacheOutput.getResultList());
		cacheOutput.sum(name, summation);
	}

	public void addFileInput(FileInput fileInput) {
		FileInputView fileInputView = new FileInputView(fileInput, this);
		this.fileInput.getChildren().add(fileInputView.getFileInputView());
		VBox.setMargin(fileInputView.getFileInputView(), new Insets(0, 0, 30, 0));
		fileInputView.getFileInputView().getStyleClass().add("file-input");
		fileInputViews.add(fileInputView);
		if (availableCrunchers != null) {
			fileInputView.updateAvailableCrunchers(availableCrunchers);
		}
		updateEnableAddFileInput();
	}

	public void removeFileInputView(FileInputView fileInputView) {
		fileInput.getChildren().remove(fileInputView.getFileInputView());
		fileInputViews.remove(fileInputView);
		updateEnableAddFileInput();
	}

	public void updateCrunchers(ArrayList<CounterCruncher> crunchers) {
		for (FileInputView fileInputView : fileInputViews) {
			fileInputView.updateAvailableCrunchers(crunchers);
		}
		this.availableCrunchers = crunchers;
	}

	public Stage getStage() {
		return stage;
	}

	private void addCruncher() {
		TextInputDialog dialog = new TextInputDialog("1");
		dialog.setTitle("Add cruncher");
		dialog.setHeaderText("Enter cruncher arity");

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(res -> {
			try {
				int arity = Integer.parseInt(res);
				for (CounterCruncher cruncher : availableCrunchers) {
					if (cruncher.getArity() == arity) {
						Alert alert = new Alert(AlertType.WARNING);
						alert.setTitle("Error");
						alert.setHeaderText("Cruncher with this arity already exists.");
						alert.setContentText(null);
						alert.showAndWait();
						return;
					}
				}
				CruncherView cruncherView = new CruncherView(this, arity, this.cacheOutput);
				this.cruncher.getChildren().add(cruncherView.getCruncherView());
				availableCrunchers.add(cruncherView.getCruncher());
				updateCrunchers(availableCrunchers);
			} catch (NumberFormatException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Wrong input");
				alert.setHeaderText("Arity must be a number");
				alert.showAndWait();
			}
		});
	}

	public void stopCrunchers() {

	}

	public void stopFileInputs() {

	}

	public void removeCruncher(CruncherView cruncherView) {
		for (FileInputView fileInputView : fileInputViews) {
			fileInputView.removeLinkedCruncher(cruncherView.getCruncher());
		}
		availableCrunchers.remove(cruncherView.getCruncher());
		updateCrunchers(availableCrunchers);
		cruncher.getChildren().remove(cruncherView.getCruncherView());
	}

	public Pane getRight() {
		return right;
	}

	public CacheOutput getCacheOutput() {
		return cacheOutput;
	}

	public ComboBox<Disk> getDisks() {
		return disks;
	}

	public HBox getLeft() {
		return left;
	}

	public VBox getFileInput() {
		return fileInput;
	}

	public VBox getCruncher() {
		return cruncher;
	}

	public Pane getCenter() {
		return center;
	}

	public ListView<String> getResults() {
		return results;
	}

	public Button getAddFileInput() {
		return addFileInput;
	}

	public Button getSumResult() {
		return sumResult;
	}

	public ArrayList<FileInputView> getFileInputViews() {
		return fileInputViews;
	}

	public static LineChart<Number, Number> getLineChart() {
		return lineChart;
	}

	public ArrayList<CounterCruncher> getAvailableCrunchers() {
		return availableCrunchers;
	}

	public ObservableList<String> getOutputResultList() {
		return outputResultList;
	}
}
