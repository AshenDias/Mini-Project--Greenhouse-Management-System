package main;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class main {

    public static void main(String[] args) {
        // Check system requirements FIRST
        if (!checkSystemRequirements()) {
            System.exit(0);
        }

        // Start the application
        SwingUtilities.invokeLater(() -> {
            try {
                view.LoginView loginView = new view.LoginView();
                loginView.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    public static boolean checkSystemRequirements() {
        StringBuilder issues = new StringBuilder();

        // Check Java version
        if (!checkJavaVersion()) {
            issues.append("• Java 8 or higher is required\n");
        }

        // Check available memory
        if (!checkMemory()) {
            issues.append("• At least 512MB RAM is recommended\n");
        }

        // Check screen resolution
        if (!checkScreenResolution()) {
            issues.append("• Minimum screen resolution: 1024x768\n");
        }

        // Check disk space
        if (!checkDiskSpace()) {
            issues.append("• At least 100MB free disk space is required\n");
        }

        // If there are issues, show warning
        if (issues.length() > 0) {
            String message = "The following issues were detected:\n\n" +
                    issues.toString() +
                    "\nThe application may not work correctly.\n" +
                    "Continue anyway?";

            int result = JOptionPane.showConfirmDialog(null, message,
                    "System Requirements Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            return result == JOptionPane.YES_OPTION;
        }

        return true;
    }

    private static boolean checkJavaVersion() {
        try {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.")) {

                double versionNum = Double.parseDouble(version.substring(0, 3));
                return versionNum >= 1.8;
            } else {

                String[] parts = version.split("\\.");
                int majorVersion = Integer.parseInt(parts[0]);
                return majorVersion >= 8;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkMemory() {
        try {
            long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024); // MB
            return maxMemory >= 512; // 512MB minimum
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean checkScreenResolution() {
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return screenSize.width >= 1024 && screenSize.height >= 768;
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean checkDiskSpace() {
        try {
            File currentDir = new File(".");
            long freeSpace = currentDir.getFreeSpace() / (1024 * 1024); // MB
            return freeSpace >= 100; // 100MB minimum
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean checkOperatingSystem() {
        String os = System.getProperty("os.name").toLowerCase();
        // Check if running on supported OS
        return os.contains("win") || os.contains("nix") || os.contains("nux") || os.contains("mac");
    }

    //Check if database driver is available

    private static boolean checkDatabaseDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return true;
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "MySQL JDBC Driver not found!\n" +
                            "Please ensure MySQL Connector/J is in the classpath.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        JLabel splashLabel = new JLabel(
                "<html><center><font size='5' color='#2E7D32'>🌿 Greenhouse Management System</font><br>" +
                        "<font size='3'>Loading...</font></center></html>",
                SwingConstants.CENTER
        );
        splashLabel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        splash.add(splashLabel);
        splash.setSize(400, 200);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        Timer timer = new Timer(2000, e -> {
            splash.dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }
}