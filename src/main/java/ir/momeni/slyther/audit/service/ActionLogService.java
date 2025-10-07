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
}
