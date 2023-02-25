package org.example.system.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

public class Maps {

    public static <K, V> K randomKey(Map<K,V> x){
        Random random = new Random();
        List<K> keys = new ArrayList<K>(x.keySet());
        return keys.get(random.nextInt(keys.size()));
    }
    /**
     * 将成对的参数组装成hashmap的方法
     *
     * @param args 键值对,第一个为键,第二个为值
     * @return 组成的map
     */
    public static <K, V> Map<K, V> newMap(Object... args) {
        Map map = new HashMap();
        if (args.length % 2 == 1)
            throw new IllegalArgumentException("参数必须成对");
        Stream.iterate(0, p -> p + 2).limit(args.length / 2)
            .forEach(p -> map.put(args[p], args[p + 1]));
        return map;
    }
    public static <K, V> Map<K, V> newLinkedMap(Object... args) {
        Map map = new LinkedHashMap();
        if (args.length % 2 == 1)
            throw new IllegalArgumentException("参数必须成对");
        Stream.iterate(0, p -> p + 2).limit(args.length / 2)
            .forEach(p -> map.put(args[p], args[p + 1]));
        return map;
    }

    /**
     * 对象转map
     * @param obj 对象
     * @param fieldStrs 选择要转的域
     * @return
     */
    public static Map<String, Object> obj2Map(Object obj,String... fieldStrs) {
        List<String> fieldNames = Arrays.asList(fieldStrs);
        Map<String, Object> map = new LinkedHashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();	// 获取f对象对应类中的所有属性域
        for (int i = 0, len = fields.length; i < len; i++) {
            String varName = fields[i].getName();
            if(!CollectionUtils.isEmpty(fieldNames) && !fieldNames.contains(varName)){
                // 若有选择域，且选择的域不包含该域，则跳过
                continue;}
            try {
                boolean accessFlag = fields[i].isAccessible();	// 获取原来的访问控制权限
                ReflectionUtils.makeAccessible(fields[i]);
                Object o = fields[i].get(obj);					// 获取在对象f中属性fields[i]对应的对象中的变量
                if (o != null){
                    map.put(varName, o.toString());
                }
                fields[i].setAccessible(accessFlag);			// 恢复访问控制权限
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
        return map;
    }
}
