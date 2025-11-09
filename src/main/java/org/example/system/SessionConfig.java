package org.example.system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

/**
 * Spring Session JDBC配置
 * 将Session存储在数据库中，支持分布式部署和Session持久化
 * 通过 application.yml 配置 save-mode 和 flush-mode
 */
@Configuration
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800) // 30分钟 = 1800秒
public class SessionConfig {

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSIONID");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        return serializer;
    }
}
