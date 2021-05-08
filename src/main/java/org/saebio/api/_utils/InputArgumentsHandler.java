package org.saebio.api._utils;

import org.apache.commons.cli.*;

public class InputArgumentsHandler {
    private Options options = new Options();
    private CommandLine commandLine;

    public InputArgumentsHandler(String[] args) {
        Option databaseOption = new Option("d", "database", true, "database file path");
        databaseOption.setRequired(true);

        options.addOption(databaseOption);

        CommandLineParser parser = new DefaultParser();
        try {
            this.commandLine = parser.parse(options, args);
        } catch(ParseException e) {
            System.err.println("Parsing failed: " + e.getMessage());
            System.exit(1);
        }
    }

    public String getOption(String option) {
        return commandLine.getOptionValue(option);
    }
}
