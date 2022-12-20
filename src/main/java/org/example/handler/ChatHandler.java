package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.constant.ChatPreset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static org.example.system.Database.userNames;

@Service
@ConditionalOnClass(SocketIOServer.class)
@Slf4j
public class ChatHandler {
    @Autowired
    SocketIOServer socketIOServer;

    @Autowired
    Gson gson;

    /**
     * 发送房间预置消息
     * */
    @OnEvent(value = "chat")
    public void roomPresetChat(SocketIOClient client, String data) {
        UUID me = client.getSessionId();
        Optional<String> room = client.getAllRooms().stream().filter(p -> !p.isEmpty()).findFirst();
        if (room.isEmpty()) {
            socketIOServer.getClient(me).sendEvent("receiveMsg", "请先加入房间！");
            return;
        }
        if(data.isEmpty()){
            StringBuilder sb = new StringBuilder();
            sb.append("可以使用的预置信息：\n");
            for (ChatPreset preset : ChatPreset.values()) {
                sb.append(preset.getId()).append("\t")
                    .append(preset.getCh()).append("\n");
            }
            socketIOServer.getClient(me).sendEvent("receiveMsg", sb.toString());
            return;
        }
        String name = userNames.get(me);

        Integer id = -1;
        try {
            id = Integer.valueOf(data);
        }catch (Exception e){}

        for (ChatPreset preset : ChatPreset.values()) {
            if(id.equals(preset.getId())){
                socketIOServer.getRoomOperations(room.get()).sendEvent(
                    "receiveMsg", "【房间】" +name+ "："+ preset.getCh());
                return;
            }
        }

        socketIOServer.getClient(me).sendEvent("receiveMsg", "输入序号错误！");
    }

    /**
     * 发送广播消息
     * */
    @OnEvent(value = "broadcastChat")
    public void broadcastChat(SocketIOClient client, String data) {
        String name = userNames.get(client.getSessionId());
        socketIOServer.getBroadcastOperations().sendEvent("broadcastChat", "【全体】" +name+ "："+ data);
    }

    /**
     * 发送房间消息
     * */
    @OnEvent(value = "roomChat")
    public void roomChat(SocketIOClient client, String data) {
        String name = userNames.get(client.getSessionId());
        for (String room : client.getAllRooms()) {
            socketIOServer.getRoomOperations(room).sendEvent("roomChat", "【房间】" +name+ "："+ data);
        }
    }

}
