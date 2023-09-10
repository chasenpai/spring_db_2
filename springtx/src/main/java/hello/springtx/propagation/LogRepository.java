package hello.springtx.propagation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class LogRepository {

    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Log logMsg) {

        log.info("log 저장");

        entityManager.persist(logMsg);

        if (logMsg.getMsg().contains("로그예외")) {
            log.info("log 저장 시 예외 발생");
            throw new RuntimeException("예외 발생");
        }
    }

    public Optional<Log> find(String logMsg) {
        return entityManager.createQuery("select l from Log l where l.msg = :msg", Log.class)
                .setParameter("msg", logMsg)
                .getResultList().stream().findAny();
    }

}
