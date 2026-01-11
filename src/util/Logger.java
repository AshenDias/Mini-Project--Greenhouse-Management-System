package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Logger {
    private static final String LOG_FILE = "greenhouse_system.log";
    private static Logger instance;

    private PrintWriter writer;

    private Logger() {
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }


    public void info(String message) {
        log("INFO", message);
    }


    public void warn(String message) {
        log("WARN", message);
    }


    public void error(String message) {
        log("ERROR", message);
    }


    public void error(String message, Exception e) {
        log("ERROR", message + " - " + e.getMessage());
        if (writer != null) {
            e.printStackTrace(writer);
        }
    }


    public void debug(String message) {
        log("DEBUG", message);
    }

    private void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] %s: %s", timestamp, level, message);


        System.out.println(logEntry);


        if (writer != null) {
            writer.println(logEntry);
        }
    }


    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
