package org.example.morecard.genshin.system;

import lombok.Getter;

/**
 * 元素骰
 */
@Getter
public enum Elemental {
    Anemo("风元素",false,true),
    Geo("岩元素",false,true),
    Electro("雷元素",true,true),
    Dendro("草元素",true,true),
    Hydro("水元素",true,true),
    Pydro("火元素",true,true),
    Cryo("冰元素",true,true),
    Universal("万能元素",false,true),
    Main("主元素",false,false),
    Void("无元素",false,false),
    ;

    private String str;
    private boolean active;// 活跃元素
    private boolean dice;// 骰子元素

    Elemental(String str, boolean active, boolean dice) {
        this.str = str;
        this.active = active;
        this.dice = dice;
    }
}
