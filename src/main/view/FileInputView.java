package main.view;

import java.io.File;
import java.util.ArrayList;

import main.input.FileInput;
import main.logger.Logger;
import main.cruncher.CounterCruncher;
import main.model.Directory;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

public class FileInputView {
	MainView mainView;
	Pane main;
	FileInput fileInput;
	ListView<CounterCruncher> linkedCrunchers;
	ListView<Directory> directories;
	ComboBox<CounterCruncher> availableCrunchers;
	Button linkCrucher;
	Button unlinkCruncher;
	Button addDirectory;
	Button removeDirectory;
	Button start;
	Button removeDiskInput;
	Text status;
	Thread fileInputComponentThread;

	public FileInputView(FileInput fileInput, MainView mainView) {
		this.mainView = mainView;
		this.fileInput = fileInput;

		main = new VBox();
		main.getChildren().add(new Text("File input " + fileInput.toString() + ": " + fileInput.getDisk().toString()));
		VBox.setMargin(main.getChildren().get(0), new Insets(0, 0, 10, 0));
		main.getChildren().add(new Text("Crunchers:"));

		int width = 210;

		linkedCrunchers = new ListView<CounterCruncher>();
		linkedCrunchers.setMinWidth(width);
		linkedCrunchers.setMaxWidth(width);
		linkedCrunchers.setMinHeight(150);
		linkedCrunchers.setMaxHeight(150);
		linkedCrunchers.getSelectionModel().selectedItemProperty().addListener(e -> updateUnlinkCruncherButtonEnabled());
		main.getChildren().add(linkedCrunchers);

		availableCrunchers = new ComboBox<CounterCruncher>();
		availableCrunchers.setMinWidth(width / 2 - 10);
		availableCrunchers.setMaxWidth(width / 2 - 10);
		availableCrunchers.getSelectionModel().selectedItemProperty().addListener(e -> updateLinkCruncherButtonEnabled());

		linkCrucher = new Button("Link cruncher");
		linkCrucher.setOnAction(e -> linkCruncher(availableCrunchers.getSelectionModel().getSelectedItem()));
		linkCrucher.setMinWidth(width / 2 - 10);
		linkCrucher.setMaxWidth(width / 2 - 10);
		linkCrucher.setDisable(true);

		HBox hBox = new HBox();
		hBox.getChildren().addAll(availableCrunchers, linkCrucher);
		HBox.setMargin(availableCrunchers, new Insets(0, 20, 0, 0));
		VBox.setMargin(hBox, new Insets(5, 0, 0, 0));
		main.getChildren().add(hBox);

		unlinkCruncher = new Button("Unlink cruncher");
		unlinkCruncher.setOnAction(e -> unlinkCruncher(linkedCrunchers.getSelectionModel().getSelectedItem()));
		unlinkCruncher.setMinWidth(width);
		unlinkCruncher.setMaxWidth(width);
		unlinkCruncher.setDisable(true);
		VBox.setMargin(unlinkCruncher, new Insets(5, 0, 0, 0));
		main.getChildren().add(unlinkCruncher);

		Text dirTitle = new Text("Dirs:");
		main.getChildren().add(dirTitle);
		VBox.setMargin(dirTitle, new Insets(10, 0, 0, 0));

		directories = new ListView<Directory>();
		directories.setMinWidth(width);
		directories.setMaxWidth(width);
		directories.setMinHeight(150);
		directories.setMaxHeight(150);
		directories.getSelectionModel().selectedItemProperty().addListener(e -> updateRemoveDirectoryButtonEnabled());
		main.getChildren().add(directories);

		addDirectory = new Button("Add dir");
		addDirectory.setOnAction(e -> addDirectory());
		addDirectory.setMinWidth(width / 2 - 10);
		addDirectory.setMaxWidth(width / 2 - 10);

		removeDirectory = new Button("Remove dir");
		removeDirectory.setOnAction(e -> removeDirectory(directories.getSelectionModel().getSelectedItem()));
		removeDirectory.setMinWidth(width / 2 - 10);
		removeDirectory.setMaxWidth(width / 2 - 10);
		removeDirectory.setDisable(true);

		hBox = new HBox();
		hBox.getChildren().addAll(addDirectory, removeDirectory);
		HBox.setMargin(addDirectory, new Insets(0, 20, 0, 0));
		VBox.setMargin(hBox, new Insets(5, 0, 0, 0));
		main.getChildren().add(hBox);

		start = new Button("Start");
		start.setOnAction(e -> {
			var isStopped = this.fileInput.getStopped().get();
			if (isStopped) {
				this.fileInput.getStopped().set(false);
				Logger.debug("First if, fileinput currently: " + this.fileInput.getStopped().get());
				start.setText("Pause");
			} else {
				this.fileInput.getStopped().set(true);
				Logger.debug("Second if, fileinput currently: " + this.fileInput.getStopped().get());
				start.setText("Start");
			}
		});
		start.setMinWidth(width);
		start.setMaxWidth(width);
		VBox.setMargin(start, new Insets(15, 0, 0, 0));
		main.getChildren().add(start);

		removeDiskInput = new Button("Remove disk input");
		removeDiskInput.setOnAction(e -> removeDiskInput());
		removeDiskInput.setMinWidth(width);
		removeDiskInput.setMaxWidth(width);
		VBox.setMargin(removeDiskInput, new Insets(5, 0, 0, 0));
		main.getChildren().add(removeDiskInput);

		status = new Text("Idle");
		status.textProperty().bind(fileInput.getWorkScheduler().messageProperty());

		VBox.setMargin(status, new Insets(5, 0, 0, 0));
		main.getChildren().add(status);

		fileInputComponentThread = new Thread(this.fileInput);
		fileInputComponentThread.start();
	}

	private void updateRemoveDirectoryButtonEnabled() {
		removeDirectory.setDisable(directories.getSelectionModel().getSelectedItem() == null);
	}

	public Pane getFileInputView() {
		return main;
	}
	
	private void updateLinkCruncherButtonEnabled() {
		CounterCruncher cruncher =  availableCrunchers.getSelectionModel().getSelectedItem();
		if(cruncher != null) {
			for(CounterCruncher linkedCruncher: linkedCrunchers.getItems()) {
				if(cruncher == linkedCruncher) {
					linkCrucher.setDisable(true);
					return;
				}
			}
			linkCrucher.setDisable(false);
		} else {
			linkCrucher.setDisable(true);
		}
	}
	
	private void updateUnlinkCruncherButtonEnabled() {
		unlinkCruncher.setDisable(linkedCrunchers.getSelectionModel().getSelectedItem() == null);
	}
	

	public void updateAvailableCrunchers(ArrayList<CounterCruncher> crunchers) {
		availableCrunchers.getItems().clear();
		if (crunchers == null || crunchers.size() == 0) {
			return;
		}
		availableCrunchers.getItems().addAll(crunchers);
		availableCrunchers.getSelectionModel().select(0);
	}

	private void linkCruncher(CounterCruncher cruncher) {
		linkedCrunchers.getItems().add(cruncher);
		this.fileInput.addCruncher(cruncher);
		updateLinkCruncherButtonEnabled();
	}
	
	public void removeLinkedCruncher(CounterCruncher cruncher) {
		linkedCrunchers.getItems().remove(cruncher);
		updateLinkCruncherButtonEnabled();
	}

	private void unlinkCruncher(CounterCruncher cruncher) {
		linkedCrunchers.getItems().remove(cruncher);
		this.fileInput.removeCruncher(cruncher);
		updateLinkCruncherButtonEnabled();
	}

	private void addDirectory() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(fileInput.getDisk().getDirectory());
		File fileDirectory = directoryChooser.showDialog(mainView.getStage());
		if (fileDirectory != null && fileDirectory.exists() && fileDirectory.isDirectory()) {
			for(Directory directory: directories.getItems()) {
				if(directory.toString().equals(fileDirectory.getPath())) {
					Alert alert = new Alert(AlertType.WARNING);
					alert.setTitle("Error");
					alert.setHeaderText("Directory: " + fileDirectory.getPath() + " is already added.");
					alert.setContentText(null);
					alert.showAndWait();
					return;
				}
			}
			Directory directory = new Directory(fileDirectory);
			directories.getItems().add(directory);
			fileInput.addDirectory(directory.directory.getAbsolutePath());
		}
	}

	private void removeDirectory(Directory directory) {
		directories.getItems().remove(directory);
		fileInput.removeDirectory(directory.directory.getAbsolutePath());
	}

	private void removeDiskInput() {
		mainView.removeFileInputView(this);
	}
	
	public void setStatus(String status) {
		this.status.setText(status);
	}
	
	public FileInput getFileInput() {
		return fileInput;
	}
}
