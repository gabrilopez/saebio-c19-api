package org.saebio.api._utils;

import org.apache.commons.cli.*;

public class InputArgumentsHandler {
    private Options options = new Options();
    private CommandLine commandLine;

    public InputArgumentsHandler(String[] args) {
        Option databaseOption = new Option("d", "database", true, "database file path");
        databaseOption.setRequired(true);

        Option databaseUserOption = new Option("u", "user", true, "database user name");
        databaseUserOption.setRequired(true);

        Option databaseUserPasswordOption = new Option("p", "password", true, "database user password");
        databaseUserPasswordOption.setRequired(true);

        options.addOption(databaseOption);
        options.addOption(databaseUserOption);
        options.addOption(databaseUserPasswordOption);

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
