package org.example.system.util;


import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;

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

    public synchronized static void story(Session session,String msg){
        try {
            session.getBasicRemote().sendObject(new Msg<>("story",msg));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized static void send(Session session,String msg){
        try {
            session.getBasicRemote().sendObject(new Msg<>(msg));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized static void warn(Session session, String msg){
        try {
            session.getBasicRemote().sendObject(new Msg<>("warn",msg));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public synchronized static void send(Session session,String channel,Object data){
        try {
            session.getBasicRemote().sendObject(new Msg<>(channel,data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
