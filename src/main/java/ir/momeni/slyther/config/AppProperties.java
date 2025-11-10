package ir.momeni.slyther.config;

import lombok.Getter; import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;


/**
 * Centralized configuration properties mapped from application configuration
 * (e.g. application.yml or application.properties).
 *
 * Loaded using prefix: {@code app.*}
 *
 * Groups:
 *  - Security settings (JWT + rate-limiting)
 *  - CORS settings
 */
@Getter @Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /** Security-related configuration (JWT + Rate limits) */
    private Security security = new Security();

    /** CORS policy configuration */
    private Cors cors = new Cors();



    /**
     * Security configuration group.
     */
    @Getter @Setter
    public static class Security {

        /** JWT generation and validation configuration */
        private Jwt jwt = new Jwt();

        /** API rate-limit configuration for selected endpoints */
        private RateLimit ratelimit = new RateLimit();


        /**
         * JWT token configuration.
         * Configurable fields under: app.security.jwt.*
         */
        @Getter @Setter public static class Jwt {

            /** Issuer name included in JWT payload */
            private String issuer;

            /** Secret key used for HS256 token signature */
            private String secret;

            /** Access token lifetime (minutes) */
            private long accessExpMins;

            /** Refresh token lifetime (days) */
            private long refreshExpDays;
        }

        /**
         * Rate limit configuration group.
         * Defines traffic limits on login / forgot password.
         */
        @Getter @Setter public static class RateLimit {

            /** Rules for /login endpoint */
            private Window login = new Window();

            /** Rules for /forgot-password endpoint */
            private Window forgotPassword = new Window();
        }

        /**
         * Defines a rate-limit time window:
         * - max allowed requests
         * - time period (in seconds)
         */
        @Getter @Setter public static class Window {

            /** Maximum allowed requests per IP/client */
            private int maxRequests = 5;

            /** Time window in seconds for request counting */
            private int windowSeconds = 60;
        }
    }


    /**
     * CORS configuration group.
     */
    @Getter @Setter
    public static class Cors {


        /**
         * Whitelisted cross-origin domain list.
         * Example values: ["https://example.com", "http://localhost:3000"]
         */
        private List<String> allowedOrigins = List.of();

        /**
         * Allowed HTTP methods for CORS requests.
         * Default includes all major HTTP verbs.
         */
        private List<String> allowedMethods = List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS");
    }
}
