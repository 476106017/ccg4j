package org.example.system;

import org.example.system.util.Msg;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.io.IOException;

import static org.example.system.Database.userNames;

@Configuration
@EnableWebSocket
public class WebSocketConfig  {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }



    public static void broadcast(String msg){
        userNames.keySet().forEach(userSession-> Msg.send(userSession,msg));
    }
}
