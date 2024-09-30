package io.sscap;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Options {

    public Ln.Level logLevel = Ln.Level.DEBUG;
    public int port = 1313;
    public int quality = 80;

    public static Options parse(String... args) {
        Options options = new Options();

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            int equalIndex = arg.indexOf('=');
            if (equalIndex == -1) {
                throw new IllegalArgumentException("Invalid key=value pair: \"" + arg + "\"");
            }
            String key = arg.substring(0, equalIndex);
            String value = arg.substring(equalIndex + 1);
            switch (key) {
                case "log_level":
                    options.logLevel = Ln.Level.valueOf(value.toUpperCase(Locale.ENGLISH));
                    break;
                case "port":
                    options.port = Integer.parseInt(value);
                    break;
                case "quality":
                    options.quality = Integer.parseInt(value);
                    break;
                default:
                    Ln.w("Unknown server option: " + key);
                    break;
            }
        }

        return options;
    }

    @NonNull
    @Override
    public String toString() {
        return "\n{" +
                "\nlog_level=" + logLevel +
                "\nport=" + port +
                "\nquality=" + quality +
                "\n}";
    }
}
