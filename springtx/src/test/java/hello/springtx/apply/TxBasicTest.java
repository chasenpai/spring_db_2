package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Slf4j
public class TxBasicTest {

    @Autowired
    BasicService basicService;

    @Test
    void proxyCheck(){
        log.info("aop class = {}", basicService.getClass());
        assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }

    @Test
    void txTest(){
        basicService.tx();
        basicService.nonTx();
    }

    @TestConfiguration
    static class TxApplyBasicConfig {

        @Bean
        BasicService basicService(){
            return new BasicService();
        }

    }

    @Slf4j
    static class BasicService {

        /**
         * @Transactional 어노테이션이 특정 클래스나 메서드에 하나라도 있으면
         * 트랜잭션 AOP 는 프록시를 만들어서 스프링 컨테이너에 등록한다
         * 그리고 프록시는 내부에 실제 BasicService 를 참조하게 된다
         * 클라이언트가 tx() 를 호출하면 프록시의 tx() 가 호출되고, 트랜잭션을 사용할 수 있는지 확인한 뒤
         * 트랜잭션을 시작한 다음에 실제 tx() 를 호출한다. 실제 tx() 의 호출이 끝나 프록시의 제어가 돌아오면
         * 프록시는 트랜잭션 로직을 커밋하거나 롤백해서 트랜잭션을 종료한다
         */
        @Transactional
        public void tx(){
            log.info("call tx");
            //현재 쓰레드에 트랜잭션이 적용되어 있는지 확인 할 수 있는 기능으로 가장 확실하게 확인 가능
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", txActive);
        }

        public void nonTx(){
            log.info("call non tx");
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active = {}", txActive);
        }

    }

}
