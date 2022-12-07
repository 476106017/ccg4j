package org.example.system;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ServerRunner implements CommandLineRunner {
    @Autowired(required = false)
    private SocketIOServer socketIOServer;

    @Override
    public void run(String... args) throws Exception {
        if (socketIOServer != null) {
            socketIOServer.start();
        }
    }
}

