
package com.github.hmdev.util;

import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

import javax.swing.JTextArea;

/** ログ出力Wrapperクラス */
public class LogAppender extends Handler {

    static JTextArea jTextArea = null;

    Formatter formatter = new SimpleFormatter();

    public LogAppender() {
    }

    public LogAppender(JTextArea _jTextArea) {
        jTextArea = _jTextArea;
    }

    public void setTarget(JTextArea _jTextArea) {
        jTextArea = _jTextArea;
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
            jTextArea.append(msg + "\n");
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (jTextArea == null || record == null) {
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
