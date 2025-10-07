package ir.momeni.slyther.session.repository;

import ir.momeni.slyther.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);
}
