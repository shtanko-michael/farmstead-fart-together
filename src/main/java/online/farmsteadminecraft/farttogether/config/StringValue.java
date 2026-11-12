package online.farmsteadminecraft.farttogether.config;

import java.util.function.Function;

/** A free-text value with an optional check ({@code null} = fine, otherwise the reason it is wrong). */
public final class StringValue extends ConfigValue<String> {
    private final Function<String, String> check;
    private final int maxLength;

    StringValue(ConfigFile file, String section, String key, String def, int maxLength, Function<String, String> check) {
        super(file, section, key, def);
        this.maxLength = maxLength;
        this.check = check == null ? s -> null : check;
    }

    public int maxLength() {
        return maxLength;
    }

    /** Sets it if valid; returns the problem otherwise (the value is kept). */
    public String set(String v) {
        String t = v == null ? "" : v.trim();
        if (t.length() > maxLength) return "longer than " + maxLength;
        String why = check.apply(t);
        if (why == null) value = t;
        return why;
    }

    @Override
    public String setRaw(String raw) {
        String t = Toml.unquote(raw.trim());
        String why = set(t);
        if (why == null) return null;
        reset();
        return problem(raw, why);
    }

    @Override
    public String text() {
        return quote(value);
    }

    @Override
    public String defaultText() {
        return quote(defaultValue());
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
