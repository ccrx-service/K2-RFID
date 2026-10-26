package dngsoftware.spoolid;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Pure-Java filtering used by the Printer > Brand > Type > Name selection.
 * Each level only looks at rows of the previous level's selection (exact, case-sensitive match;
 * no prefix or fuzzy matching). Pure Java so it can be unit tested on a plain JVM.
 *
 * Display order: ASCII-starting entries (digits/letters) first, compared case-insensitively;
 * non-ASCII (Chinese) entries after them, compared by pinyin via Collator(Locale.CHINA).
 */
final class MaterialSelect {

    /** One material record: base.brand, base.meterialType, base.id, base.name. */
    static final class Row {
        final String brand, type, id, name;

        Row(String brand, String type, String id, String name) {
            this.brand = brand;
            this.type = type;
            this.id = id;
            this.name = name;
        }
    }

    private static final Collator ZH = Collator.getInstance(Locale.CHINA);

    /** ASCII (digits/letters) first in case-insensitive order, then non-ASCII by pinyin. */
    static final Comparator<String> DISPLAY_ORDER = (a, b) -> {
        boolean aAscii = startsWithAscii(a);
        boolean bAscii = startsWithAscii(b);
        if (aAscii != bAscii) return aAscii ? -1 : 1;
        if (aAscii) return a.compareToIgnoreCase(b);
        return ZH.compare(a, b);
    };

    private static boolean startsWithAscii(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.codePointAt(0) < 128;
    }

    private MaterialSelect() {}

    static boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

    static List<String> types(List<Row> rows, String brand) {
        Set<String> out = new LinkedHashSet<>();
        for (Row r : rows) {
            if (brand != null && brand.equals(r.brand) && !blank(r.type)) out.add(r.type);
        }
        List<String> list = new ArrayList<>(out);
        list.sort(DISPLAY_ORDER);
        return list;
    }

    static List<Row> names(List<Row> rows, String brand, String type) {
        List<Row> out = new ArrayList<>();
        for (Row r : rows) {
            if (brand != null && type != null && brand.equals(r.brand) && type.equals(r.type)
                    && !blank(r.id) && !blank(r.name)) out.add(r);
        }
        out.sort(Comparator.comparing(r -> r.name, DISPLAY_ORDER));
        return out;
    }
}