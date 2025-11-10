package ir.momeni.slyther.common.api;

/**
 * Generic lightweight API response wrapper for simple success messages.
 * <p>
 * Used primarily for operations where a detailed response body is not needed,
 * such as logout confirmation or password reset acknowledgment.
 *
 * @param success indicates if the operation completed successfully
 * @param status  a human-readable status message describing the result
 */
public record ApiResponse(boolean success, String status) {}
