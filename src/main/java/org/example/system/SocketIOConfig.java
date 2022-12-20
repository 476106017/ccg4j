package org.example.system;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
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
        config.setPingTimeout(60*60*1000);
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
