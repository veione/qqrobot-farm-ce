package de.honoka.qqrobot.farm.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 快速得到Json内容
 */
public class JsonMaker {

    public static JsonObject statusAndMsg(Object status, Object msg) {
        JsonObject jo = new JsonObject();
        addUnknowValueToJsonObject(jo, "status", status);
        addUnknowValueToJsonObject(jo, "msg", msg);
        return jo;
    }

    public static JsonObject statusAndValue(Object status, Object value) {
        JsonObject jo = new JsonObject();
        addUnknowValueToJsonObject(jo, "status", status);
        addUnknowValueToJsonObject(jo, "value", value);
        return jo;    //这个JsonObject可以直接toString，得到的是未被格式化的Json字符串
    }

    /**
     * 按“键-值”的方式传递参数，组装任意Json
     */
    public static JsonObject arbitrary(Object... args) {
        JsonObject jo = new JsonObject();
        for(int i = 0; i < args.length; i++) {
            String key = args[i].toString();
            if(i + 1 < args.length) {
                i++;
                addUnknowValueToJsonObject(jo, key, args[i]);
            } else {
                addUnknowValueToJsonObject(jo, key, null);
            }
        }
        return jo;
    }

    public static Map<String, Object> arbitraryMap(Object... args) {
        Map<String, Object> map = new HashMap<>();
        for(int i = 0; i < args.length; i++) {
            String key = args[i].toString();
            if(i + 1 < args.length) {
                i++;
                map.put(key, args[i]);
            } else {
                map.put(key, null);
            }
        }
        return map;
    }

    /**
     * 将一个未知类型的数据添加到JsonObject中
     */
    private static void addUnknowValueToJsonObject(JsonObject jo, String key,
                                                   Object value) {
        //number,boolean,string,character,null
        if(value instanceof Number)
            jo.addProperty(key, (Number) value);
        else if(value instanceof Boolean)
            jo.addProperty(key, (Boolean) value);
        else if(value instanceof String)
            jo.addProperty(key, (String) value);
        else if(value instanceof Character)
            jo.addProperty(key, (Character) value);
        else if(value == null)
            jo.add(key, null);
        else if(value instanceof JsonElement)
            jo.add(key, (JsonElement) value);
        else
            jo.addProperty(key, value.toString());
    }
}