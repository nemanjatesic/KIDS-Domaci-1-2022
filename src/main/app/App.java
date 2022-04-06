package main.app;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import main.cruncher.CounterCruncher;
import main.view.CruncherView;
import main.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;


/**
 * JavaFX App
 */
public class App extends Application {
	// Must have chars that windows doesn't allow for file names for example ?
	public static final String POISON_PILL_NAME = "?POISON?";

	public static ExecutorService inputThreadPool;
	public static ExecutorService outputThreadPool;
	public static ForkJoinPool cruncherThreadPool;

    @Override
    public void start(Stage stage) {
		System.out.println(Config.getProperty("disks").split(";").length);
		inputThreadPool = Executors.newFixedThreadPool(Config.getProperty("disks").split(";").length);
		outputThreadPool = Executors.newCachedThreadPool();
		cruncherThreadPool = new ForkJoinPool();

    	BorderPane root = new BorderPane();
		Scene scene = new Scene(root, 1300, 800);
		MainView mainView = new MainView();

		stage.setOnCloseRequest(e -> App.finishApp(true));

		mainView.initMainView(root, stage);
		stage.setScene(scene);
		stage.show();
    }

    public static void finishApp(boolean showAlert) {
    	Thread thread = new Thread(() -> {
    		try {
				System.out.println("Starting to close the app");

				if (MainView.fileInputViews != null) {
					MainView.fileInputViews.forEach(fileInputView -> fileInputView.getFileInput().stop());
				}
				if (MainView.cacheOutput != null) {
					MainView.cacheOutput.stop();
				}
				if (MainView.availableCrunchers != null) {
					MainView.availableCrunchers.forEach(CounterCruncher::stop);
				}

				try {
					App.inputThreadPool.shutdown();
					App.outputThreadPool.shutdown();
					App.cruncherThreadPool.shutdown();

					App.inputThreadPool.awaitTermination(20, TimeUnit.SECONDS);
					App.outputThreadPool.awaitTermination(20, TimeUnit.SECONDS);
					App.cruncherThreadPool.awaitQuiescence(20, TimeUnit.SECONDS);

					if (CounterCruncher.threadPoolForCheckingIfTaskIsDone != null) {
						CounterCruncher.threadPoolForCheckingIfTaskIsDone.shutdown();
						CounterCruncher.threadPoolForCheckingIfTaskIsDone.awaitTermination(20, TimeUnit.SECONDS);
					}
				} catch (InterruptedException interruptedException) {
					interruptedException.printStackTrace();
					System.out.println("Force exiting");
					System.exit(0);
				}
			} catch (Exception e) {
				System.out.println("Force exiting");
    			System.exit(0);
			}
		});
		thread.start();

		if (showAlert) {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Closing");
			alert.setHeaderText("Closing the app...");
			alert.setContentText(null);
			alert.showAndWait();
		}
	}

	public static void finishAppForce() {
    	Platform.runLater(() -> {
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Closing");
			alert.setHeaderText("Closing the app...");
			alert.setContentText(null);
			alert.showAndWait();
			System.exit(0);
		});
	}
}