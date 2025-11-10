package ir.momeni.slyther.config;

import ir.momeni.slyther.audit.web.AuditInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;


/**
 * Spring Web MVC configuration.
 *
 * ✅ Registers request-level auditing interceptor for API monitoring
 * ✅ Configures CORS based on application properties
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /** Interceptor responsible for logging and auditing API requests */
    private final AuditInterceptor auditInterceptor;

    /** Application properties that include dynamic CORS configuration */
    private final AppProperties props;



    /**
     * Register application interceptors to inspect HTTP requests before controllers run.
     * <p>
     * Audit logging is applied to all API endpoints except:
     *   - /api/auth/forgot-password (security-sensitive & high frequency)
     *   - /api/health              (lightweight uptime probe)
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/forgot-password", "/api/health");    }


    /**
     * Configure CORS rules dynamically from app configuration.
     * Applied globally to all routes (`/**`).
     *
     * Allows:
     *  ✅ Custom origins
     *  ✅ Specific HTTP methods
     *  ✅ Credential support (cookies/authorization headers)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var cors = props.getCors();
        registry.addMapping("/**")
                .allowedOrigins(cors.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(cors.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
