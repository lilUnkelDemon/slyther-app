// src/main/java/ir/momeni/slyther/common/api/GlobalExceptionHandler.java
package ir.momeni.slyther.common.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Global exception handler for API controllers within selected base packages.
 * <p>
 * Produces consistent {@link ApiError} responses for common error scenarios:
 * <ul>
 *   <li>Authentication failures</li>
 *   <li>Authorization failures</li>
 *   <li>Validation errors</li>
 *   <li>Client-side mistakes (Bad Request)</li>
 *   <li>Unhandled server errors</li>
 * </ul>
 * <p>
 * Priority:
 * Marked with {@link Order LOWEST_PRECEDENCE} so that:
 * - More specific handlers can override it if needed
 * - Swagger/Actuator error handling remains intact
 */
@RestControllerAdvice(basePackages = {
        "ir.momeni.slyther.auth.controller",
        "ir.momeni.slyther.user.controller",
        "ir.momeni.slyther.test.controller"
})
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /**
     * Handles invalid credentials provided during login attempts.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCred(BadCredentialsException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Bad credentials", ex.getMessage(), req, null);
    }


    /**
     * Handles attempts to access secured resources without proper authorization.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "Access denied", ex.getMessage(), req, null);
    }


    /**
     * Handles bean validation failures from @Valid annotated request payloads.
     * <p>
     * Extracts field-level error messages into a `details` object.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage(),
                        (a,b) -> a
                ));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "Invalid request body", req, details);
    }


    /**
     * Handles common client-side exceptions thrown in service logic.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ApiError> handleBadReq(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage(), req, null);
    }


    /**
     * Fallback handler for any other unexpected exceptions.
     * <p>
     * Important:
     * - Swagger/OpenAPI routes are excluded to preserve built-in Spring error responses
     *   so developer debugging experience remains clear for documentation tools.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        // Skip custom formatting for Swagger/OpenAPI to avoid interfering with UI documentation
        String path = req.getRequestURI();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            throw new RuntimeException(ex); // rethrow so Spring's own error handler applies
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", "Unexpected error", req, null);
    }


    /**
     * Centralized builder for unified error response formatting.
     */
    private ResponseEntity<ApiError> build(HttpStatus status, String error, String message, HttpServletRequest req, Map<String, Object> details) {
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(req.getRequestURI())
                .details(details)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
