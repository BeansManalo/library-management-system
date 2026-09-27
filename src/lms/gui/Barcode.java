package lms.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal Code 39 barcode renderer -- pure Java, no external library.
 * Code 39's pattern table is small and fixed, and this project has no
 * reachable dependency repository to pull a barcode library from
 * anyway (see the network's allowed domains), so drawing it directly
 * is the appropriate amount of code for what's a genuinely simple,
 * well-defined encoding. Code 39 specifically (over other symbologies)
 * because it natively covers the uppercase-letters/digits/hyphen
 * alphabet member IDs already use (e.g. "LIB-2026-0042"), so an ID
 * encodes as-is with no reformatting.
 *
 * Table verified structurally before use: every symbol is 9 elements
 * (5 bars + 4 spaces) with exactly 3 wide -- Code 39's defining "3 of
 * 9" property -- and no two characters share a pattern.
 */
final class Barcode {

    private static final Map<Character, String> PATTERNS = new HashMap<>();
    static {
        // '0' = narrow element, '1' = wide element; 9 elements per
        // character, alternating bar/space/bar/.../bar (5 bars, 4 spaces).
        PATTERNS.put('0', "000110100");
        PATTERNS.put('1', "100100001");
        PATTERNS.put('2', "001100001");
        PATTERNS.put('3', "101100000");
        PATTERNS.put('4', "000110001");
        PATTERNS.put('5', "100110000");
        PATTERNS.put('6', "001110000");
        PATTERNS.put('7', "000100101");
        PATTERNS.put('8', "100100100");
        PATTERNS.put('9', "001100100");
        PATTERNS.put('A', "100001001");
        PATTERNS.put('B', "001001001");
        PATTERNS.put('C', "101001000");
        PATTERNS.put('D', "000011001");
        PATTERNS.put('E', "100011000");
        PATTERNS.put('F', "001011000");
        PATTERNS.put('G', "000001101");
        PATTERNS.put('H', "100001100");
        PATTERNS.put('I', "001001100");
        PATTERNS.put('J', "000011100");
        PATTERNS.put('K', "100000011");
        PATTERNS.put('L', "001000011");
        PATTERNS.put('M', "101000010");
        PATTERNS.put('N', "000010011");
        PATTERNS.put('O', "100010010");
        PATTERNS.put('P', "001010010");
        PATTERNS.put('Q', "000000111");
        PATTERNS.put('R', "100000110");
        PATTERNS.put('S', "001000110");
        PATTERNS.put('T', "000010110");
        PATTERNS.put('U', "110000001");
        PATTERNS.put('V', "011000001");
        PATTERNS.put('W', "111000000");
        PATTERNS.put('X', "010010001");
        PATTERNS.put('Y', "110010000");
        PATTERNS.put('Z', "011010000");
        PATTERNS.put('-', "010000101");
        PATTERNS.put('.', "110000100");
        PATTERNS.put(' ', "011001000");
        PATTERNS.put('*', "010010100"); // start/stop guard character
    }

    private Barcode() {
    }

    /** Draws a Code 39 barcode for {@code data} at (x, y); unsupported characters are skipped. */
    static void draw(Graphics2D g2, String data, int x, int y, int narrow, int barHeight, Color color) {
        int wide = narrow * 3;
        int cursor = x;
        g2.setColor(color);

        String encoded = "*" + data.toUpperCase() + "*";
        for (int i = 0; i < encoded.length(); i++) {
            String pattern = PATTERNS.get(encoded.charAt(i));
            if (pattern == null) {
                continue;
            }
            for (int e = 0; e < pattern.length(); e++) {
                int elementWidth = pattern.charAt(e) == '1' ? wide : narrow;
                boolean isBar = (e % 2 == 0); // elements alternate bar, space, bar, ...
                if (isBar) {
                    g2.fillRect(cursor, y, elementWidth, barHeight);
                }
                cursor += elementWidth;
            }
            cursor += narrow; // inter-character gap
        }
    }

    /** Total pixel width draw() will use for the given data, at the given narrow-bar width. */
    static int width(String data, int narrow) {
        int wide = narrow * 3;
        String encoded = "*" + data.toUpperCase() + "*";
        int total = 0;
        for (int i = 0; i < encoded.length(); i++) {
            String pattern = PATTERNS.get(encoded.charAt(i));
            if (pattern == null) {
                continue;
            }
            for (int e = 0; e < pattern.length(); e++) {
                total += pattern.charAt(e) == '1' ? wide : narrow;
            }
            total += narrow;
        }
        return total;
    }
}
