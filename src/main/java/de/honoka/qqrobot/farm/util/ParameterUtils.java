package de.honoka.qqrobot.farm.util;

import de.honoka.qqrobot.spring.boot.starter.command.CommandMethodArgs;

import java.util.Arrays;
import java.util.List;

public class ParameterUtils {

    public static int[] getSequence(int start, int end) {
        int minId = Math.min(start, end);
        int maxId = Math.max(start, end);
        int length = maxId - minId + 1;
        //长度最大为100，避免OutOfMemory
        if(length > 100) {
            maxId = minId + 99;        //min:1,  max:100,  length:100
            length = maxId - minId + 1;
        }
        int[] arr = new int[length];
        for(int i = minId; i <= maxId; i++) {
            arr[i - minId] = i;
        }
        return arr;
    }

    public static int[] toIntArr(String[] arr) {
        List<String> list = Arrays.asList(arr);
        //长度最大为100，避免OutOfMemory
        if(list.size() > 100) list = list.subList(0, 100);
        int[] intArr = new int[list.size()];
        for(int i = 0; i < list.size(); i++) {
            try {
                intArr[i] = Integer.parseInt(list.get(i));
            } catch(NumberFormatException nfe) {
                throw new CommandMethodArgs.WrongNumberParameterException();
            }
        }
        return intArr;
    }
}
