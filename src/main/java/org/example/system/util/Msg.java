package org.example.system.util;


import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Setter
@Getter
public class Msg<T> {
    private String channel;
    private T data;

    public Msg(String channel, T data) {
        this.channel = channel;
        this.data = data;
    }

    public Msg(T data) {
        this.channel = "msg";
        this.data = data;
    }

    public static void send(Session session,String msg){
        try {
            session.getBasicRemote().sendObject(new Msg<>(msg));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void send(Session session,String channel,Object data){
        try {
            session.getBasicRemote().sendObject(new Msg<>(channel,data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
