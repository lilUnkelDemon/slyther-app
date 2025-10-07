package ir.momeni.slyther.common.api;

import lombok.Builder; import lombok.Getter;
import java.time.Instant;
import java.util.Map;

@Getter @Builder
public class ApiError {
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, Object> details;
}
