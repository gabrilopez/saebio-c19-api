package org.saebio.utils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;

public class Utils {
    public static DateTimeFormatter dateTimeFormatter = createDateTimeFormatter();

    private static DateTimeFormatter createDateTimeFormatter() {
        String dateFormatterPatterns = "[d[d]-M[M]-yyyy][d[d]-M[M]-yy]";
        String patterns = dateFormatterPatterns + " " + dateFormatterPatterns.replaceAll("-", "/") + " " + dateFormatterPatterns.replaceAll("-", ".");
        DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder();
        Arrays.stream(patterns.split(" "))
                .forEach(p -> dateTimeFormatterBuilder.appendPattern(p));
        return dateTimeFormatterBuilder.toFormatter();
    }

    public static boolean isNumeric(String s) {
        return s.length() > 0 && s.chars().allMatch(Character::isDigit);
    }
}
