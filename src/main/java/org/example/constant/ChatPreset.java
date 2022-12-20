package org.example.constant;

import lombok.Getter;

@Getter
public enum ChatPreset {
    TAIYORO(11,"请指教！","対戦よろしくお願いします！"),
    ITAMI(23,"感受痛苦吧！","痛みを感じろ！"),
    CAPPUCCINO(21, "请你喝杯卡布奇诺","カプチーノをどうぞ"),
    CHECKMATE(73, "将死了！","チェックメイトだ！"),
    ;

    Integer id;
    String ch;
    String jp;

    ChatPreset(Integer id, String ch, String jp) {
        this.id = id;
        this.ch = ch;
        this.jp = jp;
    }
}
