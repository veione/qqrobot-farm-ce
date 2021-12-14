package de.honoka.qqrobot.farm.util;

import java.util.List;

public class MessageUtils {

    public static String getMultiLineMsg(List<String> list) {
        return getMultiLineMsg(list, list.size());
    }

    public static String getMultiLineMsg(List<String> list, int limit) {
        StringBuilder str = new StringBuilder();
        if(list.size() < limit) limit = list.size();
        for(int i = 0; i < limit; i++) {
            str.append(list.get(i)).append("\n");
        }
        return str.toString().trim();
    }
}
