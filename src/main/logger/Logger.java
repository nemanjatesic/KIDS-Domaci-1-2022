package main.logger;

public class Logger {
    public static boolean debugEnabled = false;

    public static void debug(String str) {
        if (debugEnabled) {
            System.out.println("DEBUG: " + str);
        }
    }

    public static void info(String str) {
        System.out.println("INFO: " + str);
    }

    public static void warning(String str) {
        System.out.println("WARNING: " + str);
    }

    public static void error(String str) {
        System.out.println("ERROR: " + str);
    }
}
