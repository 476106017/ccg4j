package org.example.game;

import lombok.Getter;


@Getter
public enum EventType{
    Destroy("破坏"),
    BackToHand("返回手牌"),
    Exile("除外"),
    ;

    private String name;

    EventType(String name) {
        this.name = name;
    }
}