
package com.github.hmdev.util;

import java.util.function.Consumer;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


/** ログ出力Wrapperクラス */
public final class LogAppender extends Handler {

    private Consumer<String> appendable;

    private final Formatter formatter = new SimpleFormatter();

    public LogAppender(Consumer<String> appendable) {
        setTarget(appendable);
    }

    public void setTarget(Consumer<String> appendable) {
        this.appendable = appendable;
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String msg;
        try {
            msg = formatter.format(record);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.FORMAT_FAILURE);
            return;
        }

        try {
            appendable.accept("<span style='color:#888888'>" + msg + "</span><br/>");
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (appendable == null || record == null) {
            return false;
        }
        return super.isLoggable(record);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
