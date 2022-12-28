package org.example.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lists {
    public static List<String> ofStr(String... ts){
        List<String> _result = new ArrayList<>();
        Collections.addAll(_result, ts);
        return _result;
    }
}
