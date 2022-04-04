package main.input;

import main.logger.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class FileReader implements Callable<FileNameAndContent> {
    private final File file;

    public FileReader(File file) {
        this.file = file;
    }

    private static String readFile(String path) {
        File file = new File(path);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return new String(data, StandardCharsets.US_ASCII);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public FileNameAndContent call() throws Exception {
        try {
            Logger.debug("[FileReader->call] Reading file:" + file.getCanonicalPath());

            String filePath = file.getAbsolutePath();
            return new FileNameAndContent(filePath, readFile(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
