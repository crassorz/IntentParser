package cc.gu.android.util.intent_parser;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cc on 2016/12/11.
 */

public class IntentParser {
//    intent:
//    HOST/URI-path // Optional host
//    #Intent;
//    package=[string];
//    action=[string];
//    category=[string];
//    component=[string];
//    scheme=[string];
//    end
//
//    String => 'S'
//    Boolean =>'B'
//    Byte => 'b'
//    Character => 'c'
//    Double => 'd'
//    Float => 'f'
//    Integer => 'i'
//    Long => 'l'
//    Short => 's'
//    S.browser_fallback_url=[encoded_full_url]

    final private static String CHOME_INTENT_REGEX = "^intent:(((?!#Intent;).)*)#Intent;(((.+=.*;)*)end)$";

    /**
     * @see <a href="https://developer.chrome.com/multidevice/android/intents#syntax</a>
     * @param input
     * @return if input is intent, then return list will have a intent to target, a intent to market(if have package), a intent to fallback_url(if have S.browser_fallback_url); else return empty list
     */

    public static List<Intent> parseChomeIntent(String input) {
        if (input == null) {
            return Collections.emptyList();
        } else if (!input.matches(CHOME_INTENT_REGEX)) {
            return Collections.emptyList();
        }
        List<Intent> intents = new ArrayList<>();
        Intent intent = new Intent();
        intents.add(intent);
        String packageName = null;
        String browser_fallback_url = null;
        Matcher m = Pattern.compile(CHOME_INTENT_REGEX).matcher(input);
        m.find(0);
        String path = m.group(1);
        System.out.println(path);
        String extrasString = m.group(3);
        System.out.println(extrasString);
        String[] extras = extrasString.split(";");
        for (int i = 0; i < extras.length - 1; i++) {
            System.out.println(extras[i]);
            int e = extras[i].indexOf("=");
            String key = extras[i].substring(0, e);
            String value = Uri.decode(extras[i].substring(e + 1));
            if (key.equals("scheme")) {
                if (path.length() > 0) {
                    path = value + ":" + path;
                } else {
                    path = value + "://";
                }
            } else if (key.equals("category")) {
                intent.addCategory(value);
            } else if (key.equals("component")) {
                ComponentName componentName = ComponentName.unflattenFromString(value);
                if (componentName != null) {
                    intent.setComponent(componentName);
                }
            } else if (key.equals("S.browser_fallback_url")) {
                browser_fallback_url = value;
            } else if (key.equals("action")) {
                intent.setAction(value);
            } else if (key.equals("package")) {
                packageName = value;
            } else if (key.matches("[SBbcdfils]\\..+")) {
                String type = key.substring(0, 1);
                key = key.substring(2);
                System.out.println(String.format("(%s)%s=%s", type, key, value));
                try {
                    switch (type) {
                        case "S":
                            intent.putExtra(key, value);
                        case "C":
                            intent.putExtra(key, (char) (value.length() == 0 ? null : value.charAt(0)));
                        case "B":
                            intent.putExtra(key, Boolean.parseBoolean(value));
                        case "b":
                            intent.putExtra(key, Byte.parseByte(value));
                        case "d":
                            intent.putExtra(key, Double.parseDouble(value));
                        case "f":
                            intent.putExtra(key, Float.parseFloat(value));
                        case "i":
                            intent.putExtra(key, Integer.parseInt(value));
                        case "l":
                            intent.putExtra(key, Long.parseLong(value));
                        case "s":
                            intent.putExtra(key, Short.parseShort(value));
                    }
                } catch (Throwable ex) {

                }
            }
        }
        if (path.length() > 0) {
            try {
                intent.setData(Uri.parse(path));
            } catch (Throwable ex) {
            }

        }
        if (packageName != null) {
            intents.addAll(parse("market://details?id=" + packageName));
        }
        if (browser_fallback_url != null) {
            intents.addAll(parse(browser_fallback_url));
        }
        return intents;
    }

    /**
     * @see Intent#getIntentOld
     * @param input
     * @return if input is url, return single list of the url intent; else return empty list
     */
    public static List<Intent> parseUrl(String input) {
        try {
            Intent intent = Intent.getIntentOld(input);
            return Collections.singletonList(intent);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * @see #parseChomeIntent(String)
     * @see #parseUrl(String)
     * @param input
     * @return
     */
    public static List<Intent> parse(String input) {
        List<Intent> intents = Collections.emptyList();
        if (input == null) {
        } else if (input.length() == 0) {
        } else if (!(intents = parseChomeIntent(input)).isEmpty()) {
        } else if (!(intents = parseUrl(input)).isEmpty()) {
        }
        return intents;
    }
}
