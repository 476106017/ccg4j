package org.example.system.util;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lists {
    public static List<String> ofStr(String... ts){
        List<String> _result = new ArrayList<>();
        Collections.addAll(_result, ts);
        return _result;
    }

    public static  <T> T randOf(List<T> list){
        if(CollectionUtils.isEmpty(list))return null;
        int size = list.size();
        int index = (int) (size * Math.random());
        return list.get(index);

    }

    public static  <T> List<T> randOf(List<T> list,int num){
        final ArrayList<T> copy = new ArrayList<>(list);
        int size = list.size();
        if(size<=num) return copy;

        Collections.shuffle(copy);
        return copy.subList(0, num);

    }
}
