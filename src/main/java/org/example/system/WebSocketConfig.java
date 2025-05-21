package org.example.system;

import org.example.system.util.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Service
@EnableWebSocket
public class WebSocketConfig  {

    @Autowired
    private GameStateService gameStateService;

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    public void broadcast(String msg){
        gameStateService.getUserNames().keySet().forEach(userSession-> Msg.send(userSession,msg));
    }
}
