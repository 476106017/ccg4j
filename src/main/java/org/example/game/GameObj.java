package org.example.game;

public abstract class GameObj {
    private static int id_iter=10000; //共用的静态变量
    public final int id;

    public GameObj() {
        id_iter++;
        id = id_iter;
    }
}
