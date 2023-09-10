package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Slf4j
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {

        @Bean
        public PlatformTransactionManager txManager(DataSource dataSource){
            return new DataSourceTransactionManager(dataSource);
        }

    }
    
    @Test
    void commit(){

        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback(){

        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void doubleCommit(){

        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);
        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션2 커밋 시작");
        txManager.commit(tx2);
        log.info("트랜잭션2 커밋 완료");
    }

    @Test
    void doubleCommitRollback(){

        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);
        log.info("트랜잭션1 커밋 완료");

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션2 롤백 시작");
        txManager.rollback(tx2);
        log.info("트랜잭션2 롤백 완료");
    }

    /**
     * 트랜잭션을 각각 사용하는 경우 각자 관리하기 때문에 전체 트랜잭션으로 묶을 수 없고
     * 트랜잭션1이 커밋되고, 트랜잭션2가 롤백될 경우 1은 커밋, 2는 롤백된다
     *
     * 외부에서 트랜잭션이 진행중인데 추가로 내부에서 트랜잭션을 수행할 경우 기존 트랜잭션을
     * 이어받을지 별도의 트랜잭션을 수행할지 동작을 결정하는 것을 트랜잭션 전파(propagation)라고 한다
     * 내부 트랜잭션이 외부 트랜잭션에 참여할 경우 하나의 물리 트랜잭션으로 묶이게 된다
     *
     * 물리 트랜잭션은 실제 데이터베이스에 적용되는 트랜잭션이고, 추가로 사용되어지는 트랜잭션을
     * 논리 트랜잭션이라 부른다
     *
     * 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋된다
     * 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션을 롤백된다
     */
    @Test
    void innerCommit() {

        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction() = {}", outer.isNewTransaction()); //true

        log.info("내부 트랜잭션 시작"); //Participating in existing transaction
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction() = {}", inner.isNewTransaction()); //false

        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner); //내부 트랜잭션은 물리 트랜잭션을 커밋하지 않는다(참여만 함)

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer); //외부 트랜잭션만 물리 트랜잭션을 시작하고 커밋한다
        //스프링은 처음 트랜잭션을 시작한 외부 트랜잭션이 실제 물리 트랜잭션을 관리하도록 한다
    }

    @Test
    void outerRollback() {

        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outer);
    }

    @Test
    void innerRollback() {

        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 롤백"); //Participating transaction failed - marking existing transaction as rollback-only
        txManager.rollback(inner); //실제 물리 트랜잭션을 롤백하지 않고 기존 트랜잭션을 롤백 전용으로 표시

        log.info("외부 트랜잭션 커밋"); //Global transaction is marked as rollback-only but transactional code requested commit
        //UnexpectedRollbackException - 스프링은 커밋을 시도했지만 롤백이 되었다고 명확하게 알려준다
        assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }
}
