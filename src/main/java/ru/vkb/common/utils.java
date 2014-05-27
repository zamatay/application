package ru.vkb.common;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Created by Zamuraev_av on 24.04.2014.
 */
public class utils {
    public static String arrayToString(String[] a){
        StringBuilder result = new StringBuilder();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i=1; i<a.length; i++) {
                result.append(",");
                result.append(a[i]);
            }
        }
        return result.toString();
    }

    public static String Encoding(String value) {
        final CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
        final StringBuilder result = new StringBuilder();
        result.append("\"");
        for (final Character character : value.toCharArray()) {
            if (asciiEncoder.canEncode(character)) {
                result.append(character);
            } else {
                result.append("\\u");
                result.append(Integer.toHexString(0x10000 | character).substring(1).toUpperCase());
            }
        }
        result.append("\"");
        return result.toString();
    }
}
