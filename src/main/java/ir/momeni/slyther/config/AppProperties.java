package ir.momeni.slyther.config;

import lombok.Getter; import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Getter @Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Security security = new Security();
    private Cors cors = new Cors();

    @Getter @Setter
    public static class Security {
        private Jwt jwt = new Jwt();
        private RateLimit ratelimit = new RateLimit();

        @Getter @Setter public static class Jwt {
            private String issuer;
            private String secret;
            private long accessExpMins;
            private long refreshExpDays;
        }
        @Getter @Setter public static class RateLimit {
            private Window login = new Window();
            private Window forgotPassword = new Window();
        }
        @Getter @Setter public static class Window {
            private int maxRequests = 5;
            private int windowSeconds = 60;
        }
    }

    @Getter @Setter
    public static class Cors {
        private List<String> allowedOrigins = List.of();
        private List<String> allowedMethods = List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS");
    }
}
