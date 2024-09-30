package io.sscap;

import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public final class Ln {

    private static final String TAG = "xcap";
    private static final String PREFIX = "[server] ";

    private static final PrintStream CONSOLE_OUT = new PrintStream(new FileOutputStream(FileDescriptor.out));
    private static final PrintStream CONSOLE_ERR = new PrintStream(new FileOutputStream(FileDescriptor.err));

    public enum Level {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }

    private static Level threshold = Level.INFO;

    private Ln() {
        // not instantiable
    }

    public static void disableSystemStreams() {
        PrintStream nullStream = new PrintStream(new NullOutputStream());
        System.setOut(nullStream);
        System.setErr(nullStream);
    }

    public static void initLogLevel(Level level) {
        threshold = level;
    }

    public static boolean isEnabled(Level level) {
        return level.ordinal() >= threshold.ordinal();
    }

    public static void v(String message) {
        if (isEnabled(Level.VERBOSE)) {
            Log.v(TAG, message);
            CONSOLE_OUT.print(PREFIX + "[" + Thread.currentThread().getName() + "] " + "VERBOSE: " + message + '\n');
        }
    }

    public static void d(String message) {
        if (isEnabled(Level.DEBUG)) {
            Log.d(TAG, message);
            CONSOLE_OUT.print(PREFIX + "[" + Thread.currentThread().getName() + "] " + "DEBUG: " + message + '\n');
        }
    }

    public static void i(String message) {
        if (isEnabled(Level.INFO)) {
            Log.i(TAG, message);
            CONSOLE_OUT.print(PREFIX + "[" + Thread.currentThread().getName() + "] " + "INFO: " + message + '\n');
        }
    }

    public static void w(String message, Throwable throwable) {
        if (isEnabled(Level.WARN)) {
            Log.w(TAG, message, throwable);
            CONSOLE_ERR.print(PREFIX + "[" + Thread.currentThread().getName() + "] " + "WARN: " + message + '\n');
            if (throwable != null) {
                throwable.printStackTrace(CONSOLE_ERR);
            }
        }
    }

    public static void w(String message) {
        w(message, null);
    }

    public static void e(String message, Throwable throwable) {
        if (isEnabled(Level.ERROR)) {
            Log.e(TAG, message, throwable);
            CONSOLE_ERR.print(PREFIX + "[" + Thread.currentThread().getName() + "] " + "ERROR: " + message + '\n');
            if (throwable != null) {
                throwable.printStackTrace(CONSOLE_ERR);
            }
        }
    }

    public static void e(String message) {
        e(message, null);
    }

    static class NullOutputStream extends OutputStream {
        @Override
        public void write(byte[] b) {
            // ignore
        }

        @Override
        public void write(byte[] b, int off, int len) {
            // ignore
        }

        @Override
        public void write(int b) {
            // ignore
        }
    }
}
