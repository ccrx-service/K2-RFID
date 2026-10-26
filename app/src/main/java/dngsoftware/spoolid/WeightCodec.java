package dngsoftware.spoolid;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Weight input validation and conversion to the Creality RFID "filamentLen" field.
 * Pure Java (no Android dependency) so it can be unit tested on a plain JVM.
 *
 * The tag does not store grams. It stores the filament length in metres as 4 digits,
 * using the original app's ratio of 0.33 m per gram (1 KG = 0330, 750 G = 0247, 600 G = 0198,
 * 500 G = 0165, 250 G = 0082), i.e. length = floor(grams * 33 / 100).
 */
final class WeightCodec {
    static final int MIN_GRAMS = 1;
    static final int MAX_GRAMS = 1000;

    private static final Pattern DIGITS = Pattern.compile("[0-9]{1,4}");
    private static final Pattern PRESET = Pattern.compile("(?i)(\\d+)\\s*(KG|G)");
    private static final Pattern LENGTH = Pattern.compile("[0-9]{4}");

    private WeightCodec() {}

    /**
     * Parses text typed or picked in the weight box into grams.
     * Accepts the five original presets ("1 KG", "750 G", "600 G", "500 G", "250 G"; spacing and case
     * are ignored) or a plain positive integer 1-1000 (grams).
     * Returns -1 for anything else (empty, 0, negative, decimals, > 1000, letters, ...).
     */
    static int parseGrams(String text) {
        if (text == null) return -1;
        String t = text.trim();
        if (t.isEmpty()) return -1;
        if (DIGITS.matcher(t).matches()) {
            int g = Integer.parseInt(t);
            return (g >= MIN_GRAMS && g <= MAX_GRAMS) ? g : -1;
        }
        java.util.regex.Matcher m = PRESET.matcher(t);
        if (m.matches()) {
            String num = m.group(1);
            boolean kg = m.group(2).equalsIgnoreCase("KG");
            if (kg && num.equals("1")) return 1000;
            if (!kg && (num.equals("750") || num.equals("600") || num.equals("500") || num.equals("250"))) {
                return Integer.parseInt(num);
            }
        }
        return -1;
    }

    /**
     * Typing filter for the weight box. Returns the text that may be inserted at [dstart, dend) of dest:
     * ASCII digits only, no leading zero, never more than 1000 in total. Deleting is always allowed.
     */
    static String acceptInsert(String dest, int dstart, int dend, String insert) {
        if (insert == null || insert.isEmpty()) return "";
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < insert.length(); i++) {
            char c = insert.charAt(i);
            if (c < '0' || c > '9') return ""; // any non-digit (typed or pasted) rejects the whole insert
            digits.append(c);
        }
        String candidate = dest.substring(0, dstart) + digits + dest.substring(dend);
        if (candidate.length() > 4 || candidate.charAt(0) == '0' || Integer.parseInt(candidate) > MAX_GRAMS) return "";
        return digits.toString();
    }

    /** Canonical preset label ("1 KG", "750 G", ...) for a valid preset input, otherwise null. */
    static String presetLabel(String text) {
        if (text == null) return null;
        java.util.regex.Matcher m = PRESET.matcher(text.trim());
        if (!m.matches()) return null;
        int g = parseGrams(text);
        switch (g) {
            case 1000: return m.group(2).equalsIgnoreCase("KG") ? "1 KG" : null;
            case 750: return "750 G";
            case 600: return "600 G";
            case 500: return "500 G";
            case 250: return "250 G";
            default: return null;
        }
    }

    /**
     * 4-digit tag length code for a gram value in 1-1000. Same ratio as the original presets.
     * Weights of 1-3 g would give 0 m, so the code is raised to 0001 (smallest non-empty length).
     * Returns null for out-of-range input.
     */
    static String lengthForGrams(int grams) {
        if (grams < MIN_GRAMS || grams > MAX_GRAMS) return null;
        int metres = grams * 33 / 100;
        if (metres < 1) metres = 1;
        return String.format(Locale.US, "%04d", metres);
    }

    /**
     * Weight text for a length code read from a tag: the exact preset label if the code is one of the
     * five preset codes, otherwise the smallest gram value that encodes to this length (the tag keeps
     * metres, so custom weights come back to within a few grams). Returns null when the code is not
     * 4 digits or maps outside 1-1000 g.
     */
    static String labelForLength(String length) {
        if (length == null || !LENGTH.matcher(length).matches()) return null;
        switch (length) {
            case "0330": return "1 KG";
            case "0247": return "750 G";
            case "0198": return "600 G";
            case "0165": return "500 G";
            case "0082": return "250 G";
            default: break;
        }
        int metres = Integer.parseInt(length);
        if (metres < 1) return null;
        int grams = (metres * 100 + 32) / 33; // ceil(metres / 0.33)
        if (grams < MIN_GRAMS || grams > MAX_GRAMS) return null;
        return String.valueOf(grams);
    }
}
