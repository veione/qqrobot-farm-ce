package de.honoka.qqrobot.farm.util;

import java.util.function.Consumer;

public class IdGenerator {

    /**
     * 从1开始多次尝试以某个ID插入一条记录，直到插入成功，或到达最大ID为止
     */
    public static Long generateLong(long maxId, Consumer<Long> generateMethod) {
        for(long id = 1; id <= maxId; id++) {
            try {
                //尝试以某个ID插入一条记录
                generateMethod.accept(id);
                //成功插入，id即获取成功
                return id;
            } catch(Throwable t) {
                //id已被占用，检索下一个id是否可用
            }
        }
        //没有可用id
        return null;
    }

    public static Integer generateInteger(
            int maxId, Consumer<Integer> generateMethod) {
        Consumer<Long> consumer = id -> generateMethod.accept(Math.toIntExact(id));
        Long generateId = generateLong(maxId, consumer);
        if(generateId == null) return null;
        return Math.toIntExact(generateId);
    }
}
