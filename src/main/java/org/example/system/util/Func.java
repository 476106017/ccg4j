package org.example.system.util;

import com.google.gson.Gson;

public class Func {

    public static String toJson(Object src) {
        return SpringContext.getBean(Gson.class).toJson(src, src.getClass());
    }
}
