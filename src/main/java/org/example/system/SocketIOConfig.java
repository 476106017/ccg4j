package org.example.system;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import com.corundumstudio.socketio.listener.ConnectListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketIOConfig {

    @Value("${server.wss-port}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setOrigin(null);// 跨域
        config.setPort(port);
        config.setAllowCustomRequests(true);
        config.getSocketConfig().setReuseAddress(true);
        SocketIOServer server = new SocketIOServer(config);
        return server;
    }
    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer ssrv) {
        return new SpringAnnotationScanner(ssrv);
    }

}
