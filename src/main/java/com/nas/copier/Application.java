package com.nas.copier;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nas.copier.util.ApplicationConfig;
import com.nas.copier.util.ApplicationConstants;

public class Application {
    public static void main(String[] args) {
        try {
            System.out.println("NASCopier process started at: " + getCurrentDateTime());
            String configFilePath = new File(Application.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent() + File.separator + "config.properties";
            if (!new ApplicationConfig().loadApplicationProperties(configFilePath)) {
                return;
            }
            String sourceLocation = ApplicationConfig.getTag(ApplicationConstants.SOURCE_LOCATION);
            String remoteLocation = ApplicationConfig.getTag(ApplicationConstants.REMOTE_LOCATION);
            String logLocation = ApplicationConfig.getTag(ApplicationConstants.LOG_LOCATION);
            if (logLocation == null || logLocation.isEmpty()) {
                System.err.println("LOG_LOCATION is not defined in the configuration file.");
                return;
            }
            if (!logLocation.endsWith(File.separator)) {
                logLocation += File.separator;
            }
            String logPath = logLocation + "Prod_" + getCurrentDate() + ".log";
            if (!validateDirectory(sourceLocation, "Source Location")) {
                System.err.println("Source location is not accessible. Exiting...");
                return;
            }
            if (!validateDirectory(remoteLocation, "Remote Location")) {
                System.err.println("Remote location is not accessible. Exiting...");
                return;
            }
            File logFile = new File(logPath);
            File logDir = logFile.getParentFile();
            if (logDir != null && !logDir.exists()) {
                System.out.println("Log directory does not exist. Attempting to create: " + logDir.getAbsolutePath());
                if (!logDir.mkdirs()) {
                    System.err.println("Failed to create log directory: " + logDir.getAbsolutePath());
                    System.err.println("Please check if the application has write permissions for this location.");
                    return;
                }
            }

            if (!logFile.exists()) {
                try {
                    if (!logFile.createNewFile()) {
                        System.err.println("Failed to create log file: " + logFile.getAbsolutePath());
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Error creating log file: " + logFile.getAbsolutePath());
                    e.printStackTrace();
                    return;
                }
            }

            ProcessBuilder processBuilder = new ProcessBuilder("robocopy", sourceLocation, remoteLocation, "/E", "/MIR");
            processBuilder.redirectErrorStream(true);

            System.out.println("Starting NASCopier process...");
            Process process = processBuilder.start();

            // Capture the output of the robocopy process
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {

                String line;
                int lineCount = 0;

                while ((line = reader.readLine()) != null) {
                    // Skip the first 4 lines
                    if (lineCount >= 4) {
                        writer.write(line);
                        writer.newLine();
                    }
                    lineCount++;
                }
            }

            int exitCode = process.waitFor();
            String endTime = getCurrentDateTime();
            System.out.println("NASCopier process completed at: " + endTime);
            System.out.println("---------------------------------------------------------------");
            if (exitCode == 0) {
                System.out.println("Integrity check passed: The directories are identical.");
            } else if (exitCode == 1) {
                System.err.println("Sync process finished. The directories are now in sync");
            } else {
                System.out.println("NASCopier exited with code: " + exitCode);
            }
            System.out.println("---------------------------------------------------------------");
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private static boolean validateDirectory(String path, String dirName) {
        File dir = new File(path);
        if (!dir.exists()) {
            System.err.println(dirName + " does not exist: " + path);
            return false;
        }
        if (!dir.isDirectory()) {
            System.err.println(dirName + " is not a valid directory: " + path);
            return false;
        }
        if (!dir.canRead()) {
            System.err.println(dirName + " is not readable: " + path);
            return false;
        }
        if (!dir.canWrite()) {
            System.err.println(dirName + " is not writable: " + path);
            return false;
        }
        System.out.println(dirName + " is accessible: " + path);
        return true;
    }

    private static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(new Date());
    }

    private static String getCurrentDateTime() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateTimeFormat.format(new Date());
    }
}