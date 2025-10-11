package ir.momeni.slyther.audit.service;

import ir.momeni.slyther.audit.entity.ActionLog;
import ir.momeni.slyther.audit.repository.ActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActionLogService {
    private final ActionLogRepository repo;

    public void save(ActionLog log) { repo.save(log); }

    /** لاگ عمومی (غیر HTTP): method=APP و status=0 تا با محدودیت DB سازگار باشد */
    public void info(String msg, String path, String username, String ip) {
        repo.save(ActionLog.builder()
                .method("APP")            // ⬅️ مهم: null نیست
                .status(0)               // primitive int، 0 اوکیه
                .msg(msg)
                .path(path)
                .username(username)
                .ip(ip)
                .success(true)
                .build());
    }

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

    /** (اختیاری) نسخهٔ HTTP-محور برای وقتی می‌خوای خودت method/status رو مشخص کنی */
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
