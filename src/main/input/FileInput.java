package main.input;

import main.cruncher.CounterCruncher;
import main.logger.Logger;
import main.model.Disk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class FileInput implements Runnable {
//    public static ExecutorService workSchedulerThreadPool = Executors.newCachedThreadPool();

    private final int sleepTime;
    private final Map<String, Long> fileToLastEdited = new ConcurrentHashMap<>();
    private final List<String> directories = new ArrayList<>();
    private final BlockingQueue<String> filesToRead = new LinkedBlockingQueue<>();
    private boolean shouldRun = true;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final WorkScheduler workScheduler;
    private final Disk disk;
    private final String name;
    private final AtomicBoolean stopped = new AtomicBoolean(true);
    private final List<CounterCruncher> crunchers = new CopyOnWriteArrayList<>();

    private Thread workSchedulerThread;

    public FileInput(int sleepTime, Disk disk) {
        this.sleepTime = sleepTime;
        this.name = "0";
        this.disk = disk;
        this.workScheduler = new WorkScheduler(this.filesToRead, this.stopped, this.crunchers);
    }

    @Override
    public void run() {
        Logger.info("Started running FileInput, thread name: " + Thread.currentThread().getName() + ", thread id: " + Thread.currentThread().getId());

        workSchedulerThread = new Thread(workScheduler);
        workSchedulerThread.start();

        final List<String> coppiedDirs = new ArrayList<>();
        while (this.shouldRun) {
            if (stopped.get())
                continue;

            Logger.debug("Starting FileInput run");
            // Clear list use of this
            coppiedDirs.clear();

            lock.readLock().lock();
            coppiedDirs.addAll(directories);
            lock.readLock().unlock();

            for (var path : coppiedDirs) {
                File file = new File(path);
                iterateOverDirectory(file);
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                if (Logger.debugEnabled) {
                    e.printStackTrace();
                }
            }

            Logger.debug("Finishing FileInput run");
        }
        Logger.info("Finished running FileInput, thread name: " + Thread.currentThread().getName() + ", thread id: " + Thread.currentThread().getId());

        workScheduler.stop();
    }

    public void addDirectory(String directory) {
        lock.writeLock().lock();

        File file = new File(directory);

        if (!file.exists()) {
            Logger.error("That folder with this path doesn't exist, path: " + directory);
            lock.writeLock().unlock();
            return;
        }
        if (!file.isDirectory()) {
            Logger.error("Given path doesn't lead to the folder, path: " + directory);
            lock.writeLock().unlock();
            return;
        }
        if (directories.contains(file.getAbsolutePath())) {
            Logger.error("Path already exists, path: " + directory);
            lock.writeLock().unlock();
            return;
        }

        directories.add(file.getAbsolutePath());
        Logger.info("Added " + file.getAbsolutePath() + " to paths");

        lock.writeLock().unlock();
    }

    public void removeDirectory(String directory) {
        lock.writeLock().lock();

        directories.remove(directory);
        var list = fileToLastEdited.keySet().stream().filter(path -> path.contains(directory)).collect(Collectors.toList());
        list.forEach(fileToLastEdited::remove);

        lock.writeLock().unlock();
    }

    public void addCruncher(CounterCruncher cruncher) {
        if (!this.crunchers.contains(cruncher)) {
            this.crunchers.add(cruncher);
        }
    }

    public void removeCruncher(CounterCruncher cruncher) {
        this.crunchers.remove(cruncher);
    }

    private void iterateOverDirectory(File root) {
        if (root == null) {
            return;
        }
        for (String path : Objects.requireNonNull(root.list())) {
            File file = new File(root.getAbsolutePath() + File.separator + path);

            if (file.isDirectory()) {
                this.iterateOverDirectory(file);
                continue;
            }

            this.handleFile(file);
        }
    }

    private void handleFile(File file) {
        boolean shouldStartJob = false;
        if (!fileToLastEdited.containsKey(file.getAbsolutePath())) {
            fileToLastEdited.put(file.getAbsolutePath(), file.lastModified());
            shouldStartJob = true;
        } else {
            if (fileToLastEdited.get(file.getAbsolutePath()) != file.lastModified()) {
                fileToLastEdited.put(file.getAbsolutePath(), file.lastModified());
                shouldStartJob = true;
            }
        }

        if (shouldStartJob) {
            initNewJob(file);
        }
    }

    private void initNewJob(File file) {
        this.filesToRead.add(file.getAbsolutePath());
    }

    public void stop() {
        this.shouldRun = false;
    }

    public Disk getDisk() {
        return disk;
    }
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public WorkScheduler getWorkScheduler() {
        return workScheduler;
    }

    public AtomicBoolean getStopped() {
        return stopped;
    }

    public Thread getWorkSchedulerThread() {
        return workSchedulerThread;
    }
}
