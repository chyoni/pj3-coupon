package cwchoiit.couponcore;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@SpringBootTest(classes = CouponCoreConfiguration.class)
@TestPropertySource(properties = "spring.config.name=application-core")
public class TestConfigurations {
}
