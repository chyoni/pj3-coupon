package cwchoiit.couponcore;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.config.name=application-core")
public class EmbeddedRedis {
    private final RedisServer redisServer;

    public EmbeddedRedis() throws IOException {
        this.redisServer = new RedisServer(63790);
    }

    @PostConstruct
    public void onStart() throws IOException {
        this.redisServer.start();
    }

    @PreDestroy
    public void onStop() throws IOException {
        this.redisServer.stop();
    }
}
