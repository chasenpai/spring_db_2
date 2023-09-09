package hello.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
@Slf4j
public class InternalCallV2Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy(){
        log.info("callService class = {}", callService.getClass());
    }

    @Test
    void externalCallV2(){
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService(){
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService(){
            return new InternalService();
        }

    }

    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external(){
            log.info("call external");
            printTxInfo();
            //주입받은 InternalService 는 트랜잭션 프록시이므로 트랜잭션을 적용한다
            internalService.internal();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", txActive);
        }

    }

    static class InternalService {

        /**
         * 스프링의 트랜잭션 AOP 는 public 메서드만 적용하도록 기본 설정 되어있다
         * 만약 클래스 레벨에 트랜잭션을 적용하면 모든 메서드에 걸릴 수 있게 된다
         * 그러면 트랜잭션을 의도하지 않는 곳 까지 과도하게 적용될 수 있다
         * 트랜잭션은 주로 비즈니스 로직의 시작점에 걸기 때문에 보통 외부에 열어준 곳을 시작점으로 사용한다
         * 이러한 이유로 public 메서드에만 트랜잭션을 적용하도록 설정되어 있다
         */
        @Transactional
        public void internal(){
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", txActive);
        }

    }

}
