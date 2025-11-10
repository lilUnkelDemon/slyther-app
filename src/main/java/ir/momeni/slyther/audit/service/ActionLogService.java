package ir.momeni.slyther.audit.service;

import ir.momeni.slyther.audit.entity.ActionLog;
import ir.momeni.slyther.audit.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * Service class responsible for logging actions and events into the database.
 * Provides different methods for logging info, warnings, and errors
 * in both general and HTTP-specific contexts.
 */
@Service
@RequiredArgsConstructor
public class ActionLogService {
    private final ActionLogRepository repo;


    /**
     * Persists a given ActionLog entity to the database.
     *
     * @param log The ActionLog object to be saved.
     */
    public void save(ActionLog log) { repo.save(log); }


    /**
     * Logs a general info-level message (non-HTTP).
     * Uses default method "APP" and status 0.
     *
     * @param msg      The log message.
     * @param path     The source path or action being logged.
     * @param username The username associated with the action.
     * @param ip       The IP address of the requester.
     */
    public void info(String msg, String path, String username, String ip) {
        repo.save(ActionLog.builder()
                .method("APP")            // Default method for non-HTTP logs
                .status(0)               // Default status code
                .msg(msg)
                .path(path)
                .username(username)
                .ip(ip)
                .success(true)
                .build());
    }


    /**
     * Logs a general warning-level message (non-HTTP).
     * Internally uses the same structure as the info method.
     *
     * @param msg      The warning message.
     * @param path     The source path or action being logged.
     * @param username The username associated with the action.
     * @param ip       The IP address of the requester.
     */
    public void warn(String msg, String path, String username, String ip) {
        repo.save(ActionLog.builder()
                .method("APP")
                .status(0)
                .msg(msg)
                .path(path)
                .username(username)
                .ip(ip)
                .success(true)
                .build());
    }

    /**
     * Logs an error-level message (non-HTTP), including an exception message if present.
     *
     * @param msg      The error message.
     * @param ex       The throwable exception related to the error (can be null).
     * @param path     The source path or action being logged.
     * @param username The username associated with the action.
     * @param ip       The IP address of the requester.
     */
    public void error(String msg, Throwable ex, String path, String username, String ip) {
        repo.save(ActionLog.builder()
                .method("APP")
                .status(0)
                .msg(msg)
                .errorMessage(ex != null ? ex.getMessage() : null)
                .path(path)
                .username(username)
                .ip(ip)
                .success(false)
                .build());
    }


    /**
     * Logs an HTTP-based info-level message with customizable HTTP method and status code.
     * Useful when the context is tied to an HTTP request.
     *
     * @param msg      The log message.
     * @param method   The HTTP method used (e.g., GET, POST). Defaults to "APP" if null.
     * @param status   The HTTP status code (e.g., 200, 404).
     * @param path     The URL or path being accessed.
     * @param username The username associated with the request.
     * @param ip       The IP address of the client.
     */
    public void infoHttp(String msg, String method, int status, String path, String username, String ip) {
        repo.save(ActionLog.builder()
                .method(method != null ? method : "APP")
                .status(status)
                .msg(msg)
                .path(path)
                .username(username)
                .ip(ip)
                .success(status < 400)
                .build());
    }



    /**
     * Logs an HTTP-based error message with exception details.
     *
     * @param msg      The error message.
     * @param ex       The exception related to the error (can be null).
     * @param method   The HTTP method used (e.g., GET, POST). Defaults to "APP" if null.
     * @param status   The HTTP status code associated with the error.
     * @param path     The URL or path that caused the error.
     * @param username The username associated with the request.
     * @param ip       The IP address of the client.
     */
    public void errorHttp(String msg, Throwable ex, String method, int status, String path, String username, String ip) {
        repo.save(ActionLog.builder()
                .method(method != null ? method : "APP")
                .status(status)
                .msg(msg)
                .errorMessage(ex != null ? ex.getMessage() : null)
                .path(path)
                .username(username)
                .ip(ip)
                .success(false)
                .build());
    }
}
