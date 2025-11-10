package ir.momeni.slyther.common.api;

import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.Map;

/**
 * Standard error response model used for formatted API error output.
 * <p>
 * Designed to:
 * - Provide consistent error structure for all controllers
 * - Support rich details for validation and domain-level issues
 * - Improve observability/debuggability through timestamp + path
 * <p>
 * Typical usage:
 * Returned by global exception handlers to provide a unified error contract.
 */
@Getter
@Builder
public class ApiError {

    /** Timestamp when the error occurred (UTC-based). */
    private final Instant timestamp;

    /** HTTP status code (e.g., 400, 404, 500). */
    private final int status;

    /** Short title describing the type of error (derived from status). */
    private final String error;

    /** Human-readable explanation of the error cause. */
    private final String message;

    /** Request path on which the error occurred. */
    private final String path;

    /**
     * Additional optional error metadata, useful for:
     * - Bean validation errors
     * - Context-specific diagnostic codes
     * - Debug information in development
     */
    private final Map<String, Object> details;
}
