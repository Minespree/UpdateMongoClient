package net.minespree.migrations;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @since 28/10/2017
 */
public class LogFormatter extends Formatter {
    private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);

        builder.append("[");
        builder.append(df.format(new Date(System.currentTimeMillis())));
        builder.append("] [Migrations] [").append(record.getLevel()).append("] ");

        builder.append(formatMessage(record)).append("\n");
        return builder.toString();
    }
}
