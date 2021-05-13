package org.saebio.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager {
    private static final Logger logger;

    static {
        logger = Logger.getLogger("Logs");
        char slash = File.separatorChar;
        String directory = "." + slash + "logs";

        FileHandler fileHandler;
        try {
            new File(directory).mkdir();
            String fileName = new SimpleDateFormat("dd-MM-yyyy").format(new Date()) + ".log";
            fileHandler = new FileHandler(directory + slash + fileName, true);
            logger.addHandler(fileHandler);
            fileHandler.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void info(String message) {
        logger.info(message + "\n");
    }

    public static void warning(String message) {
        logger.warning(message + "\n");
    }

    public static void error(String message, Object params) {
        logger.log(Level.SEVERE, message + "\n", params);
    }

    public static void error(String message) {
        logger.log(Level.SEVERE, message + "\n");
    }
}
