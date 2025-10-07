package ir.momeni.slyther.audit.repository;

import ir.momeni.slyther.audit.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionLogRepository extends JpaRepository<ActionLog, Long> { }
