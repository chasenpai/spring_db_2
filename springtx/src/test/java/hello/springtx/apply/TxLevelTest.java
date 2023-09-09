package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class TxLevelTest {

    @Autowired
    LevelService levelService;

    @Test
    void orderTest() {
        levelService.write();
        levelService.read();
    }

    @TestConfiguration
    static class TxLevelTestConfig {

        @Bean
        LevelService levelService(){
            return new LevelService();
        }

    }

    @Slf4j
    @Transactional(readOnly = true)
    static class LevelService {

        /**
         * 스프링에서 우선순위는 항상 더 구체적이고 자세한 것이 높은 우선순위를 가진다
         * 클래스 보다는 메서드가 더 구체적이기 때문에 readOnly = false 옵션을 사용한 트랜잭션이 적용된다
         * 만약 인터페이스에  @Transactional 적용한다면?
         * 클래스의 메서드 > 클래스의 타입 > 인터페이스의 메서드 > 인터페이스의 타입
         */
        @Transactional(readOnly = false)
        public void write(){
            log.info("call write");
            printTxInfo();
        }

        //클래스에 @Transactional 적용하면 메서드는 자동 적용
        public void read(){
            log.info("call read");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", txActive);
            //현재 트랜잭션의 속성이 read only 인지 확인 가능
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly = {}", readOnly);
        }

    }

}
