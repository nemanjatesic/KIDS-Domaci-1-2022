package main.cruncher;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import main.app.App;
import main.app.Config;
import main.input.FileNameAndContent;
import main.logger.Logger;
import main.output.BOWFutureAndFileName;
import main.output.CacheOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CounterCruncher implements Runnable {
	public static ExecutorService threadPoolForCheckingIfTaskIsDone = Executors.newCachedThreadPool();

	private final int arity;
	private final String name;
	private final int counterLimit;
	private final ObservableList<String> crunchingFilePaths;
	private final BlockingQueue<FileNameAndContent> inputQue;
	private final CopyOnWriteArrayList<CacheOutput> outputs = new CopyOnWriteArrayList<>();

	public CounterCruncher(int arity, ObservableList<String> crunchingFilePaths) {
		this.crunchingFilePaths = crunchingFilePaths;
		this.arity = arity;
		this.name = String.format("Cruncher %d", arity);
		this.counterLimit = Integer.parseInt(Config.getProperty("counter_data_limit"));
		this.inputQue = new LinkedBlockingQueue<>();
	}

	@Override
	public void run() {
		Logger.info("Started running CounterCruncher, thread name: " + Thread.currentThread().getName()
				+ ", thread id: " + Thread.currentThread().getId());
		FileNameAndContent currentFileNameAndContent;
		while (true) {
			try {
				currentFileNameAndContent = inputQue.take();

				if (currentFileNameAndContent.getFilePath().equals(App.POISON_PILL_NAME)) {
					break;
				}

				// Must replace \\ with / since for some reason split can't take \\
				String filePathString = currentFileNameAndContent.getFilePath().replace("\\", "/");
				String[] split = filePathString.split("/");
				String fileName = split[split.length - 1];

				// Will work without it but we get an exception
				Platform.runLater(() -> crunchingFilePaths.add(fileName));

				String currentContent = currentFileNameAndContent.getContent();
				int fileContentLen = currentContent.length();
				int defaultChunkSize = counterLimit;
				int numberOfChunks = (int) Math.ceil(fileContentLen * 1.0D / defaultChunkSize);
				List<Pair<Integer, Integer>> startEndPoints = new CopyOnWriteArrayList<>();

				int currentlyLast = 0;
				for (int i = 0; i < numberOfChunks; i++) {
					int currentStart = currentlyLast;
					int currentEnd = currentStart + defaultChunkSize;

					if (currentEnd > fileContentLen) {
						currentlyLast = fileContentLen;
						startEndPoints.add(new Pair<>(currentStart, fileContentLen));
						continue;
					}

					char currentLastChar = currentContent.charAt(currentEnd);
					while (currentLastChar != ' ') {
						currentEnd++;
						currentLastChar = currentContent.charAt(currentEnd);
					}

					currentlyLast = currentEnd;
					startEndPoints.add(new Pair<>(currentStart, currentlyLast));
				}
				startEndPoints.forEach(System.out::println);

				Future<Map<ListOfWords<Integer>, Integer>> future = App.cruncherThreadPool.submit(new BagOfWordsTask(
						arity, counterLimit, 0, startEndPoints.size(), currentContent, startEndPoints));

				List<CacheOutput> copiedCacheOutputs = new ArrayList<>(this.outputs);
				for (CacheOutput cacheOutput : copiedCacheOutputs) {
					String name = String.format("%s-arity%d", fileName, arity);
					BOWFutureAndFileName futureAndFileName = new BOWFutureAndFileName(name, future);
					if (cacheOutput.getFilePathToResult().get(name) == null) {
						cacheOutput.getInputQue().put(futureAndFileName);
						String tmp = name.replace("\\", "/");
						String[] splitTmp = tmp.split("/");
						String formattedStringForOutput = "*" + splitTmp[splitTmp.length - 1];
						Platform.runLater(() -> cacheOutput.getResultList().add(formattedStringForOutput));
					} else {

					}

					threadPoolForCheckingIfTaskIsDone.submit(new CheckIfTaskDone(futureAndFileName,
							cacheOutput.getResultList(), crunchingFilePaths, fileName, cacheOutput.getInputQue()));
				}
				currentFileNameAndContent = null;
				System.gc();

				//// Future<Map<ListOfWords<Integer>, Integer>> future =
				//// App.cruncherThreadPool.submit(new BagOfWordsTask(arity, counterLimit, 0,
				//// currentFileNameAndContent.getContent().length(),
				//// currentFileNameAndContent.getContent()));
				//// output.keySet().stream().sorted((a, b) -> output.get(b) -
				//// output.get(a)).limit(100).forEach(str ->
				//// Logger.debug(String.format("STRING: [%s], value: [%d]",
				//// str.getList().get(0), output.get(str))));
				////
				////
				// // TODO: Delete this
				// Map<ListOfWords<Integer>, Integer> output = future.get();
				// List<ListOfWords<Integer>> list = output.keySet().stream().sorted((a, b) ->
				//// output.get(b) - output.get(a)).limit(100).collect(Collectors.toList());
				//// list.forEach(low -> {
				//// low.getList().forEach(str -> System.out.print(str + ", "));
				//// System.out.print(" -> ");
				//// System.out.println(output.get(low));
				//// });
				// XYChart.Series<Number, Number> series = new XYChart.Series<>();
				// for (int i = 0 ; i < list.size() ; i++) {
				// series.getData().add(new XYChart.Data<>(i, output.get(list.get(i))));
				// }
				//
				// Platform.runLater(() -> {
				// MainView.lineChart.getData().clear();
				// MainView.lineChart.getData().addAll(series);
				// });
				// // TODO: Delete this
			} catch (Exception e) {
				if (Logger.debugEnabled) {
					e.printStackTrace();
				}
				return;
			}
		}
		Logger.info("Finished running CounterCruncher, thread name: " + Thread.currentThread().getName()
				+ ", thread id: " + Thread.currentThread().getId());
	}

	@Override
	public String toString() {
		return name;
	}

	public int getArity() {
		return arity;
	}

	public String getName() {
		return name;
	}

	public int getCounterLimit() {
		return counterLimit;
	}

	public List<String> getCrunchingFilePaths() {
		return crunchingFilePaths;
	}

	public BlockingQueue<FileNameAndContent> getInputQue() {
		return inputQue;
	}

	public void addOutput(CacheOutput cacheOutput) {
		if (!this.outputs.contains(cacheOutput)) {
			this.outputs.add(cacheOutput);
		}
	}

	public void stop() {
		try {
			FileNameAndContent currentFileNameAndContent = new FileNameAndContent(App.POISON_PILL_NAME, null);
			inputQue.put(currentFileNameAndContent);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
