package org.example.card.genshin;

import lombok.Getter;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 元素骰
 */
@Getter
public enum Elemental {
    Anemo("风元素",false),
    Geo("岩元素",false),
    Electro("雷元素",true),
    Dendro("草元素",true),
    Hydro("水元素",true),
    Pydro("火元素",true),
    Cryo("冰元素",true),
    Universal("万能元素",false),
    Main("主元素",false),
    Void("无元素",false),
    ;

    private String str;
    private boolean active;// 活跃元素

    Elemental(String str, boolean active) {
        this.str = str;
        this.active = active;
    }
}
