package org.example.endpoint.handler;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.constant.ChatPreset;
import org.example.system.util.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.example.system.Database.userNames;
import static org.example.system.Database.userRoom;

@Service
@Slf4j
public class ChatHandler {

    @Autowired
    Gson gson;

    /**
     * 发送房间预置消息
     * */

    public void chat(Session client, String data) throws IOException {
        final String room = userRoom.get(client);
        if (Strings.isBlank(room)) {
            Msg.send(client,"请先加入房间！");
            return;
        }
        if(data.isEmpty()){
            StringBuilder sb = new StringBuilder();
            sb.append("可以使用的预置信息：\n");
            for (ChatPreset preset : ChatPreset.values()) {
                sb.append(preset.getId()).append("\t")
                    .append(preset.getCh()).append("\n");
            }
Msg.send(client,sb.toString());
            return;
        }
        String name = userNames.get(client);

        Integer id = -1;
        try {
            id = Integer.valueOf(data);
        }catch (Exception e){}

        for (ChatPreset preset : ChatPreset.values()) {
            if(id.equals(preset.getId())){
                userRoom.forEach((k,v)->{if(room.equals(v)) {
                    Msg.send(k,"【房间】" +name+ "："+ preset.getCh());
                }
                });
                return;
            }
        }

        Msg.send(client,"输入序号错误！");
    }


}
