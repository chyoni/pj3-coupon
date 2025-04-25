package cwchoiit.couponcore.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributeLockExecutor {

    private final RedissonClient redissonClient;

    public void execute(String lockName, long waitMillis, long timeout, Runnable runnable) {
        RLock lock = redissonClient.getLock(lockName);

        try {
            boolean isLocked = lock.tryLock(waitMillis, timeout, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new IllegalStateException("Lock is not acquired");
            }
            runnable.run();
        } catch (InterruptedException e) {
            log.error("[execute] Lock is interrupted", e);
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
