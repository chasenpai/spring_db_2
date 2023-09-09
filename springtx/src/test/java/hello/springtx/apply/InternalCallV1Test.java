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
@Slf4j
public class InternalCallV1Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy(){
        log.info("callService class = {}", callService.getClass());
    }

    @Test
    void internalCall(){
        callService.internal();
    }

    @Test
    void externalCall(){
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService(){
            return new CallService();
        }

    }

    static class CallService {

        /**
         * 트랜잭션 AOP 주의 사항 - 프록시 내부 호출
         * - 트랜잭션 AOP 는 기본적으로 프록시 방식이기 때문에 트랜잭션을 적용하려면
         * - 항상 프록시를 통해서 대상 객체를 호출해야 한다
         * - 만약 프록시를 거치지 않고 대상 객체를 직접 호출하면 AOP 가 적용되지 않고, 트랜잭션도 적용되지 않는다
         */
        public void external(){
            log.info("call external");
            printTxInfo();
            /**
             * internal 메서드에 @Transactional 이 선언되어 있지만 대상 객체 내부에서 호출이 발생하기 때문에
             * 프록시를 거치지 않고 대상 객체를 직접 호출하는 문제가 발생한다
             * 이는 프록시 방식의 AOP 한계로, 프록시를 사용하면 메서드 내부 호출에 프록시를 적용할 수 없다
             * 간단한 해결 방법은 internal 메서드를 별도의 클래스로 분리하는 것이다
             */
            internal();
        }

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
