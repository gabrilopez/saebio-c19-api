package org.saebio.api._utils;

import org.apache.commons.cli.*;
import org.saebio.utils.LogManager;

public class InputArgumentsHandler {
    private Options options = new Options();
    private CommandLine commandLine;

    public InputArgumentsHandler(String[] args) {
        Option databaseOption = new Option("d", "database", true, "database file path");
        databaseOption.setRequired(true);

        Option episodeLengthOption = new Option("e", "episode-length", true, "episode length in days");
        episodeLengthOption.setRequired(true);
        episodeLengthOption.setType(Number.class);

        Option portOption = new Option("p", "port", true, "port used by Java Spark server");
        portOption.setRequired(true);
        portOption.setType(Number.class);

        options.addOption(databaseOption);
        options.addOption(episodeLengthOption);
        options.addOption(portOption);

        CommandLineParser parser = new DefaultParser();
        try {
            this.commandLine = parser.parse(options, args);
        } catch(ParseException e) {
            System.err.println("Parsing failed: " + e.getMessage());
            System.exit(1);
        }
    }

    public String getDatabaseRoute() {
        return commandLine.getOptionValue("database");
    }

    public int getEpisodeLength() {
        if (commandLine.hasOption("episode-length")) {
            try {
                return ((Number) commandLine.getParsedOptionValue("episode-length")).intValue();
            } catch (ParseException e) {
                LogManager.error("InputArgumentsHandler::getEpisodeLength::" + e.toString(), e);
                System.err.println("Parsing failed: " + e.getMessage());
                System.exit(1);
            }
        }
        return -1;
    }

    public int getPort() {
        if (commandLine.hasOption("episode-length")) {
            try {
                return ((Number) commandLine.getParsedOptionValue("port")).intValue();
            } catch (ParseException e) {
                LogManager.error("InputArgumentsHandler::getPort::" + e.toString(), e);
                System.err.println("Parsing failed: " + e.getMessage());
                System.exit(1);
            }
        }
        return -1;
    }
}
