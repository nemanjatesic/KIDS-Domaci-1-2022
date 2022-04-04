package main.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import main.cruncher.CounterCruncher;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import main.output.CacheOutput;

public class CruncherView {
	private final MainView mainView;
	private final CounterCruncher cruncher;

	private final Pane main;
	private final ObservableList<String> crunchingFilePaths;

	private final CacheOutput cacheOutput;

	private final Thread cruncherComponentThread;

	public CruncherView(MainView mainView, int arity, CacheOutput cacheOutput) {
		this.cacheOutput = cacheOutput;
		this.mainView = mainView;
		this.crunchingFilePaths = FXCollections.observableArrayList();
		var crunchListView = new ListView<>(this.crunchingFilePaths);

		this.cruncher = new CounterCruncher(arity, this.crunchingFilePaths);
		this.cruncher.addOutput(cacheOutput);

		main = new VBox();

		Text text = new Text("Name: " + cruncher.toString());
		main.getChildren().add(text);
		VBox.setMargin(text, new Insets(0, 0, 2, 0));

		text = new Text("Arity: " + cruncher.getArity());
		main.getChildren().add(text);
		VBox.setMargin(text, new Insets(0, 0, 5, 0));

		Button remove = new Button("Remove cruncher");
		remove.setOnAction(e -> removeCruncher());
		main.getChildren().add(remove);
		VBox.setMargin(remove, new Insets(0, 0, 5, 0));

		Text status = new Text("Crunching:");
		main.getChildren().add(status);

		crunchListView.setMaxWidth(120);
		crunchListView.setMaxHeight(200);

		main.getChildren().add(crunchListView);

		VBox.setMargin(main, new Insets(0, 0, 15, 0));

		cruncherComponentThread = new Thread(cruncher);
		cruncherComponentThread.start();
	}

	public Pane getCruncherView() {
		return main;
	}

	private void removeCruncher() {
		mainView.removeCruncher(this);
	}

	public CounterCruncher getCruncher() {
		return cruncher;
	}

	public MainView getMainView() {
		return mainView;
	}

	public ObservableList<String> getCrunchingFilePaths() {
		return crunchingFilePaths;
	}

	public Thread getCruncherComponentThread() {
		return cruncherComponentThread;
	}
}
