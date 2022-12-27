package org.example.game;


import lombok.Data;
import org.example.card.Card;

@Data
public abstract class GameObj {
    private static int id_iter=10000; //共用的静态变量
    public final int id;

    public abstract String getName();

    public GameObj() {
        id_iter++;
        id = id_iter;
    }
}
