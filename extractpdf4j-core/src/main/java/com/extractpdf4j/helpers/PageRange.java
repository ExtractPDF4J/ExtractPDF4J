package com.extractpdf4j.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * PageRange
 *
 * <p>Parses human-friendly page range expressions into integer lists used by parsers.
 * Supports single pages (e.g., {@code 1}), ranges (e.g., {@code 2-5}), mixed lists
 * (e.g., {@code 1,3-4}), and {@code all} (represented by a single {@code -1}).</p>
 */
public final class PageRange {
    private PageRange(){}
    public static List<Integer> parse(String expr) {
        if (expr == null || expr.isBlank()) expr = "1";
        expr = expr.trim().toLowerCase();
        List<Integer> out = new ArrayList<>();
        if ("all".equals(expr)) { out.add(-1); return out; }
        String[] parts = expr.split(",");
        for (String p : parts) {
            p = p.trim();
            if (p.isBlank()) continue;
            if (p.contains("-")) {
                String[] ab = p.split("-");
                int a = Integer.parseInt(ab[0].trim());
                int b = Integer.parseInt(ab[1].trim());
                for (int i=a;i<=b;i++) out.add(i);
            } else {
                out.add(Integer.parseInt(p));
            }
        }
        return out;
    }
}
