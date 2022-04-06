package main.input;

import javafx.concurrent.Task;
import main.app.App;
import main.cruncher.CounterCruncher;
import main.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkScheduler extends Task<String> {
    public static final String INITIAL_STATE_STRING = "Idle";
    private final BlockingQueue<String> filesToRead;
    private String currentlyReadingFile;
    private final AtomicBoolean stopped;
    private final List<CounterCruncher> crunchers;

    public WorkScheduler(BlockingQueue<String> filesToRead, AtomicBoolean stopped, List<CounterCruncher> crunchers) {
        this.filesToRead = filesToRead;
        this.stopped = stopped;
        this.currentlyReadingFile = INITIAL_STATE_STRING;
        this.crunchers = crunchers;
    }

    @Override
    public void run() {
        Logger.info("Started running WorkScheduler, thread name: " + Thread.currentThread().getName() + ", thread id: " + Thread.currentThread().getId());

        updateMessage(INITIAL_STATE_STRING);

        while (true) {
            try {
                if (stopped.get())
                    continue;

                currentlyReadingFile = filesToRead.take();

                if (currentlyReadingFile.equals(App.POISON_PILL_NAME)) {
                    break;
                }

                // Must replace \\ with / since for some reason split can't take \\
                String prettyFormatPath = currentlyReadingFile.replace("\\", "/");
                String[] split = prettyFormatPath.split("/");
                prettyFormatPath = split[split.length - 1];

                updateMessage(prettyFormatPath);

                try {
                    Future<FileNameAndContent> fileReadFuture = App.inputThreadPool.submit(new FileReader(new File(currentlyReadingFile)));
                    FileNameAndContent fileContent = fileReadFuture.get();

                    if (fileContent == null) {
                        updateMessage(INITIAL_STATE_STRING);
                        break;
                    }

                    List<CounterCruncher> copyListOfCrunchers = new ArrayList<>(this.crunchers);
                    for (CounterCruncher cruncher : copyListOfCrunchers) {
                        cruncher.getInputQue().add(fileContent);
                    }

                    currentlyReadingFile = INITIAL_STATE_STRING;
                    updateMessage(INITIAL_STATE_STRING);
                } catch (Exception e) {
                }
            } catch (Exception e) {
                this.currentlyReadingFile = "Idle";
                updateMessage(this.currentlyReadingFile);
                e.printStackTrace();
                return;
            }
        }

        Logger.info("Finished running WorkScheduler, thread name: " + Thread.currentThread().getName() + ", thread id: " + Thread.currentThread().getId());
    }

    @Override
    protected String call() throws Exception {
        return null;
    }

    public void stop() {
        try {
            filesToRead.put(App.POISON_PILL_NAME);
            stopped.set(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
