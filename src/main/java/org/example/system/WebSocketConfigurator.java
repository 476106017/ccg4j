package org.example.system;

import jakarta.websocket.server.ServerEndpointConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnWebApplication
@Configuration
public class WebSocketConfigurator {
    @Bean
    public CustomSpringConfigurator customSpringConfigurator() {
        return new CustomSpringConfigurator(); // This is just to get context
    }


    public static class CustomSpringConfigurator extends ServerEndpointConfig.Configurator implements ApplicationContextAware {

        /**
         * Spring application context.
         */
        private static volatile BeanFactory context;

        @Override
        public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
            return context.getBean(clazz);
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            CustomSpringConfigurator.context = applicationContext;
        }
    }
}