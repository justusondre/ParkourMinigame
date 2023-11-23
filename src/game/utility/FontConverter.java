package game.utility;

import java.util.HashMap;
import java.util.Map;

public class FontConverter {
	private static final Map<Character, String> fontMap = new HashMap<>();

    static {
        fontMap.put('a', "ᴀ");
        fontMap.put('b', "ʙ");
        fontMap.put('c', "ᴄ");
        fontMap.put('d', "ᴅ");
        fontMap.put('e', "ᴇ");
        fontMap.put('f', "ꜰ");
        fontMap.put('g', "ɢ");
        fontMap.put('h', "ʜ");
        fontMap.put('i', "ɪ");
        fontMap.put('j', "ᴊ");
        fontMap.put('k', "ᴋ");
        fontMap.put('l', "ʟ");
        fontMap.put('m', "ᴍ");
        fontMap.put('n', "ɴ");
        fontMap.put('o', "ᴏ");
        fontMap.put('p', "ᴘ");
        fontMap.put('q', "ǫ");
        fontMap.put('r', "ʀ");
        fontMap.put('s', "ѕ");
        fontMap.put('t', "ᴛ");
        fontMap.put('u', "ᴜ");
        fontMap.put('v', "ᴠ");
        fontMap.put('w', "ᴡ");
        fontMap.put('x', "х");
        fontMap.put('y', "ʏ");
        fontMap.put('z', "ᴢ");
        fontMap.put('1', "₁");
        fontMap.put('2', "₂");
        fontMap.put('3', "₃");
        fontMap.put('4', "₄");
        fontMap.put('5', "₅");
        fontMap.put('6', "₆");
        fontMap.put('7', "₇");
        fontMap.put('8', "₈");
        fontMap.put('9', "₉");
        fontMap.put('0', "₀");
    }

    public static String convertFont(String text) {
        text = text.toLowerCase(); // Convert text to lowercase for simplicity
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fontMap.containsKey(c)) {
                result.append(fontMap.get(c)).append(" ");
            } else if (Character.isDigit(c)) {
                result.append(fontMap.get(c)).append(" ");
            } else {
                result.append(" ");
            }
        }
        return result.toString();
    }

    public static String convertInt(int number) {
        return convertFont(Integer.toString(number));
    }
}